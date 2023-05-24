package cn.crisp.crispmaintenanceuser.service.impl;

import cn.crisp.common.Constants;
import cn.crisp.common.R;
import cn.crisp.crispmaintenanceuser.es.ESService;
import cn.crisp.crispmaintenanceuser.mapper.UserMapper;
import cn.crisp.crispmaintenanceuser.security.service.TokenService;
import cn.crisp.crispmaintenanceuser.service.UserService;
import cn.crisp.crispmaintenanceuser.utils.RedisCache;
import cn.crisp.dto.LoginDto;
import cn.crisp.dto.MailUpdateDto;
import cn.crisp.entity.User;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Transactional
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService{
    @Autowired
    RedisCache redisCache;

    @Autowired
    ESService esService;

    @Autowired
    UserMapper userMapper;

    @Autowired
    BCryptPasswordEncoder encoder;

    @Autowired
    TokenService tokenService;

    @Autowired
    RedissonClient redissonClient;

    private void crispCopyUser(User source, User target) {
        if (target.getIcon() != null) {
            source.setIcon(target.getIcon());
        }

        if (target.getPhone() != null) {
            source.setPhone(target.getPhone());
        }

        if (target.getMail() != null) {
            source.setMail(target.getMail());
        }

        if (target.getUsername() != null) {
            source.setUsername(target.getUsername());
        }

        if (target.getRole() != null) {
            source.setRole(target.getRole());
        }
    }

    /**
     *检查Email 格式（正则表达式）
     * @param content
     * @return
     */
    private boolean checkEmailFormat(String content){
        /*
         * " \w"：匹配字母、数字、下划线。等价于'[A-Za-z0-9_]'。
         * "|"  : 或的意思，就是二选一
         * "*" : 出现0次或者多次
         * "+" : 出现1次或者多次
         * "{n,m}" : 至少出现n个，最多出现m个
         * "$" : 以前面的字符结束
         */
        String REGEX="^\\w+((-\\w+)|(\\.\\w+))*@\\w+(\\.\\w{2,3}){1,3}$";
        Pattern p = Pattern.compile(REGEX);
        Matcher matcher=p.matcher(content);

        return matcher.matches();
    }



    //判断手机号是否违规
    public  boolean isMobile(String mobiles) {
        Pattern p = Pattern.compile("^((13[0-9])|(14[0|5|6|7|9])|(15[0-3])|(15[5-9])|(16[6|7])|(17[2|3|5|6|7|8])|(18[0-9])|(19[1|8|9]))\\d{8}$");
        Matcher m = p.matcher(mobiles);
        return m.matches();
    }

    @Override
    public R<String> register(LoginDto loginDto) {
        if (loginDto.getPhone() == null || loginDto.getPhone().length() == 0 || loginDto.getPassword().length() == 0 || loginDto.getPassword() == null){
            return R.error("参数错误");
        }

        if (!isMobile(loginDto.getPhone())) {
            return R.error("手机号格式错误");
        }
        //将数据库的修改和es的转为原子操作
        RLock lock = redissonClient.getLock(Constants.USER_LOCK_NAME + loginDto.getPhone());
        //阻塞式等待，默认为30s过期时间，业务过长会自动续期，加锁业务执行完（通过线程判断）不会自动续期，30s后过期
        lock.lock();
        try {
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getPhone, loginDto.getPhone());
            if (userMapper.selectOne(wrapper) != null) return R.error("手机号已被注册");
            User user = new User();
            user.setId(null);
            user.setPhone(loginDto.getPhone());
            user.setPassword(encoder.encode(loginDto.getPassword()));
            user.setUsername(loginDto.getPhone());


            this.save(user);
            //这里获取id存入到es中
            user = this.getOne(wrapper);
            esService.docInsert(user, user.getId().toString(), Constants.USER_ES_INDEX_NAME);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            lock.unlock();
        }
        return R.success("添加成功");
    }

    @Override
    public User selectByPhone(String phone) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone);
        return userMapper.selectOne(wrapper);
    }

    @Override
    public R<User> getByToken(HttpServletRequest request) {
        User user = tokenService.getLoginUser(request).getUser();
        if (user == null) {
            return R.error("用户信息错误，请重新登录");
        }
        return R.success(user);
    }

    @Override
    public User selectById(Long id) {
        return esService.docGet(id.toString(),Constants.USER_ES_INDEX_NAME, User.class);
    }

    @Override
    public R<User> updateOne(HttpServletRequest request, User user) {
        User user3 = tokenService.getLoginUser(request).getUser();
        //试图修改别的用户
        if (!Objects.equals(user.getId(), user3.getId())) {
            return null;
        }
        //将数据库的修改和es的转为原子操作
        RLock lock = redissonClient.getLock(Constants.USER_LOCK_NAME + user.getId().toString());
        //阻塞式等待，默认为30s过期时间，业务过长会自动续期，加锁业务执行完（通过线程判断）不会自动续期，30s后过期
        lock.lock();
        User ret = null;
        try {
            ret = userMapper.selectById(user.getId());
            if (ret == null) {
                return R.error("用户信息错误");
            }
            crispCopyUser(ret, user);
            //这里不设置为空不会自动更新
            ret.setUpdateTime(null);
            if (!this.updateById(ret)) return R.error("更新失败");
            ret = userMapper.selectById(user.getId());
            esService.docInsert(ret, ret.getId().toString(), Constants.USER_ES_INDEX_NAME);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            lock.unlock();
        }
        return R.success(ret);
    }

    @Override
    public R<User> updatePhone(HttpServletRequest request, User user) {
        //电话不合格
        if (!isMobile(user.getPhone())) {
            return R.error("电话格式错误");
        }

        //不存在用户
        User user1 = userMapper.selectById(user.getId());
        if (user1 == null) {
            return R.error("用户不存在");
        }

        User user3 = tokenService.getLoginUser(request).getUser();
        //试图修改别的用户
        if (!Objects.equals(user.getId(), user3.getId())) {
            return R.error("无法修改其他用户信息");
        }

        //手机号已经存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, user.getPhone());
        User user2 = userMapper.selectOne(wrapper);
        if (user2 != null) return R.error("电话已经被使用");

        return this.updateOne(request, user);

    }


    @Override
    public R<User> updateMail(HttpServletRequest request, MailUpdateDto mailUpdateDto) {
        if (!checkEmailFormat(mailUpdateDto.getMail())) {
            return R.error("邮箱不合规");
        }

        User user = tokenService.getLoginUser(request).getUser();
        if (user == null) return R.error("登录信息错误");
        if (!(user.getId().equals(mailUpdateDto.getId()))) return R.error("无法修改其他用户");

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getMail, mailUpdateDto.getMail());
        User user1 = userMapper.selectOne(wrapper);
        if (user1 != null) return R.error("邮箱已经被使用");

        String code = redisCache.getCacheObject(Constants.VALIDATE_MAIL_KEY + mailUpdateDto.getMail());
        if (!code.equals(mailUpdateDto.getCode())){
            return R.error("验证码错误");
        }

        user.setMail(mailUpdateDto.getMail());

        return this.updateOne(request, user);
    }


}
