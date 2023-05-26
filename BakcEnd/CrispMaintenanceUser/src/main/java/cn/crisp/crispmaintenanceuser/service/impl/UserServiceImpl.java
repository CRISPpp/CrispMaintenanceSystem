package cn.crisp.crispmaintenanceuser.service.impl;

import cn.crisp.common.Constants;
import cn.crisp.common.R;
import cn.crisp.crispmaintenanceuser.entity.ESMap;
import cn.crisp.crispmaintenanceuser.entity.LoginUser;
import cn.crisp.crispmaintenanceuser.es.ESService;
import cn.crisp.crispmaintenanceuser.mapper.UserMapper;
import cn.crisp.crispmaintenanceuser.security.service.TokenService;
import cn.crisp.crispmaintenanceuser.service.EngineerAttributeService;
import cn.crisp.crispmaintenanceuser.service.UserAttributeService;
import cn.crisp.crispmaintenanceuser.service.UserService;
import cn.crisp.crispmaintenanceuser.utils.RedisCache;
import cn.crisp.dto.*;
import cn.crisp.entity.Address;
import cn.crisp.entity.EngineerAttribute;
import cn.crisp.entity.User;
import cn.crisp.entity.UserAttribute;
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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Transactional
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService{
    @Autowired
    private RedisCache redisCache;

    @Autowired
    private ESService esService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private BCryptPasswordEncoder encoder;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private UserAttributeService userAttributeService;

    @Autowired
    private EngineerAttributeService engineerAttributeService;

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
    public R<String> register(RegisterDto registerDto) {
        if (registerDto.getPhone() == null || registerDto.getPhone().length() == 0 || registerDto.getPassword().length() == 0 || registerDto.getPassword() == null){
            return R.error("参数错误");
        }

        if (!isMobile(registerDto.getPhone())) {
            return R.error("手机号格式错误");
        }
        //将数据库的修改和es的转为原子操作
        RLock lock = redissonClient.getLock(Constants.USER_LOCK_NAME + registerDto.getPhone());
        //阻塞式等待，默认为30s过期时间，业务过长会自动续期，加锁业务执行完（通过线程判断）不会自动续期，30s后过期
        lock.lock();
        try {
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getPhone, registerDto.getPhone());
            if (userMapper.selectOne(wrapper) != null) return R.error("手机号已被注册");
            User user = new User();
            user.setId(null);
            user.setPhone(registerDto.getPhone());
            user.setPassword(encoder.encode(registerDto.getPassword()));
            user.setUsername(registerDto.getPhone());
            if (registerDto.getRole() != null && (registerDto.getRole().equals(1) || registerDto.getRole().equals(2))) {
                user.setRole(registerDto.getRole());
            }

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

    @Override
    public R<User> updatePassword(HttpServletRequest request, PasswordUpdateDto passwordUpdateDto) {
        User user = tokenService.getLoginUser(request).getUser();
        if (user == null) return R.error("登录信息错误");
        if (!(user.getId().equals(passwordUpdateDto.getId()))) return R.error("无法修改其他用户");
        if (!user.getPassword().equals(encoder.encode(passwordUpdateDto.getOldPassword()))) {
            return R.error("原密码错误");
        }
        user.setPassword(encoder.encode(passwordUpdateDto.getNewPassword()));
        return this.updateOne(request, user);
    }

    @Override
    public R<Boolean> pay(PayDto payDto) {
        ESMap<Long> userMap= new ESMap<>("userId", payDto.getUserId());
        List<ESMap> list = new ArrayList<>();
        list.add(userMap);
        ESMap<Long> engineerMap = new ESMap<>("userId", payDto.getEngineerId());
        List<ESMap> list1 = new ArrayList<>();
        list1.add(engineerMap);

        List<UserAttribute> userAttributeList = esService.docGet(Constants.USER_ATTRIBUTE_ES_INDEX_NAME, UserAttribute.class, list);
        List<EngineerAttribute> engineerAttributeList = esService.docGet(Constants.ENGINEER_ATTRIBUTE_ES_INDEX_NAME, EngineerAttribute.class, list1);

        if (userAttributeList == null || userAttributeList.size() != 1) return R.error("用户属性错误");
        if (engineerAttributeList == null || engineerAttributeList.size() != 1) return R.error("工程师属性错误");

        UserAttribute userAttribute = userAttributeList.get(0);
        EngineerAttribute engineerAttribute = engineerAttributeList.get(0);

        if (userAttribute.getBalance().compareTo(payDto.getMoney()) < 0) {
            return R.error("用户余额不足");
        }

        userAttribute.setBalance(userAttribute.getBalance().subtract(payDto.getMoney()));
        engineerAttribute.setBalance(engineerAttribute.getBalance().add(payDto.getMoney().multiply(new BigDecimal("0.7")).multiply(engineerAttribute.getQuality()).divide(new BigDecimal("5.0"), 2, BigDecimal.ROUND_HALF_UP)));
        userAttribute.setUpdateTime(null);
        engineerAttribute.setUpdateTime(null);

        //将数据库的修改和es的转为原子操作
        RLock lock = redissonClient.getLock(Constants.USER_ATTRIBUTE_LOCK_NAME + userAttribute.getId().toString());
        //阻塞式等待，默认为30s过期时间，业务过长会自动续期，加锁业务执行完（通过线程判断）不会自动续期，30s后过期
        lock.lock();
        RLock lock1 = redissonClient.getLock(Constants.ENGINEER_ATTRIBUTE_LOCK_NAME + engineerAttribute.getId().toString());
        lock1.lock();
        Boolean ret = true;
        try {
           ret = userAttributeService.updateById(userAttribute);
           esService.docInsert(userAttribute, userAttribute.getId().toString(), Constants.USER_ATTRIBUTE_ES_INDEX_NAME);
           ret &= engineerAttributeService.updateById(engineerAttribute);
           esService.docInsert(engineerAttribute, engineerAttribute.getId().toString(), Constants.ENGINEER_ATTRIBUTE_ES_INDEX_NAME);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            lock.unlock();
            lock1.unlock();
        }

        return R.success(ret);
    }

    @Override
    public R<UserAttribute> addUserAttribute(HttpServletRequest request, UserAttribute userAttribute) {
        User user = tokenService.getLoginUser(request).getUser();
        if (user.getRole() != 1) {
            return R.error("操作者为工程师");
        }
        userAttribute.setUserId(user.getId());
        //将数据库的修改和es的转为原子操作
        RLock lock = redissonClient.getLock(Constants.USER_ATTRIBUTE_LOCK_NAME + user.getId().toString());
        //阻塞式等待，默认为30s过期时间，业务过长会自动续期，加锁业务执行完（通过线程判断）不会自动续期，30s后过期
        lock.lock();
        UserAttribute ret = null;
        try {
            LambdaQueryWrapper<UserAttribute> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserAttribute::getUserId, user.getId());

            if (userAttributeService.getOne(wrapper) != null) return R.error("用户属性已存在");

            userAttributeService.save(userAttribute);

            ret = userAttributeService.getOne(wrapper);

            esService.docInsert(ret, ret.getId().toString(), Constants.USER_ATTRIBUTE_ES_INDEX_NAME);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            lock.unlock();
        }
        return R.success(ret);
    }

    @Override
    public R<EngineerAttribute> addEngineerAttribute(HttpServletRequest request, EngineerAttribute engineerAttribute) {
        User user = tokenService.getLoginUser(request).getUser();
        if (user.getRole() != 2) {
            return R.error("操作者为普通用户");
        }
        engineerAttribute.setUserId(user.getId());
        //将数据库的修改和es的转为原子操作
        RLock lock = redissonClient.getLock(Constants.ENGINEER_ATTRIBUTE_LOCK_NAME + user.getId().toString());
        //阻塞式等待，默认为30s过期时间，业务过长会自动续期，加锁业务执行完（通过线程判断）不会自动续期，30s后过期
        lock.lock();
        EngineerAttribute ret = null;
        try {
            LambdaQueryWrapper<EngineerAttribute> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(EngineerAttribute::getUserId, user.getId());

            if (engineerAttributeService.getOne(wrapper) != null) return R.error("用户属性已存在");

            engineerAttributeService.save(engineerAttribute);

            ret = engineerAttributeService.getOne(wrapper);

            esService.docInsert(ret, ret.getId().toString(), Constants.ENGINEER_ATTRIBUTE_ES_INDEX_NAME);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            lock.unlock();
        }
        return R.success(ret);

    }

    @Override
    public R<UserAttribute> updateUserAttribute(HttpServletRequest request, UserAttribute userAttribute) {
        User user = tokenService.getLoginUser(request).getUser();
        if (!userAttribute.getUserId().equals(user.getId())) return R.error("无法修改其他用户的信息");
        //将数据库的修改和es的转为原子操作
        RLock lock = redissonClient.getLock(Constants.USER_ATTRIBUTE_LOCK_NAME + userAttribute.getId().toString());
        //阻塞式等待，默认为30s过期时间，业务过长会自动续期，加锁业务执行完（通过线程判断）不会自动续期，30s后过期
        lock.lock();
        UserAttribute ret = null;
        try {
            ret = esService.docGet(userAttribute.getId().toString(),Constants.USER_ATTRIBUTE_ES_INDEX_NAME ,UserAttribute.class);
            if (ret == null) {
                return R.error("用户属性不存在");
            }
            ret.setBalance(userAttribute.getBalance());
            ret.setUpdateTime(null);
            userAttributeService.updateById(userAttribute);
            ret = userAttributeService.getById(userAttribute.getId());

            esService.docInsert(ret, ret.getId().toString(), Constants.USER_ATTRIBUTE_ES_INDEX_NAME);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            lock.unlock();
        }
        return R.success(ret);
    }

    @Override
    public R<EngineerAttribute> updateEngineerAttribute(HttpServletRequest request, EngineerAttribute engineerAttribute) {
        User user = tokenService.getLoginUser(request).getUser();
        if (!engineerAttribute.getUserId().equals(user.getId())) return R.error("无法修改其他用户的信息");
        //将数据库的修改和es的转为原子操作
        RLock lock = redissonClient.getLock(Constants.ENGINEER_ATTRIBUTE_LOCK_NAME + engineerAttribute.getId().toString());
        //阻塞式等待，默认为30s过期时间，业务过长会自动续期，加锁业务执行完（通过线程判断）不会自动续期，30s后过期
        lock.lock();
        EngineerAttribute ret = null;
        try {
            ret = esService.docGet(engineerAttribute.getId().toString(),Constants.ENGINEER_ATTRIBUTE_ES_INDEX_NAME ,EngineerAttribute.class);
            if (ret == null) {
                return R.error("用户属性不存在");
            }
            ret.setBalance(engineerAttribute.getBalance());
            ret.setQuality(engineerAttribute.getQuality());
            ret.setUpdateTime(null);
            engineerAttributeService.updateById(engineerAttribute);
            ret = engineerAttributeService.getById(engineerAttribute.getId());

            esService.docInsert(ret, ret.getId().toString(), Constants.ENGINEER_ATTRIBUTE_ES_INDEX_NAME);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            lock.unlock();
        }
        return R.success(ret);
    }


}
