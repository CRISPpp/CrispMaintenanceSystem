package cn.crisp.crispmaintenanceuser.service.impl;

import cn.crisp.common.Constants;
import cn.crisp.common.R;
import cn.crisp.crispmaintenanceuser.es.ESService;
import cn.crisp.crispmaintenanceuser.mapper.UserMapper;
import cn.crisp.crispmaintenanceuser.security.service.TokenService;
import cn.crisp.crispmaintenanceuser.service.UserService;
import cn.crisp.crispmaintenanceuser.utils.RedisCache;
import cn.crisp.dto.LoginDto;
import cn.crisp.entity.User;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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


    //判断手机号是否违规
    public  boolean isMobile(String mobiles) {
        Pattern p = Pattern.compile("^((13[0-9])|(14[0|5|6|7|9])|(15[0-3])|(15[5-9])|(16[6|7])|(17[2|3|5|6|7|8])|(18[0-9])|(19[1|8|9]))\\d{8}$");
        Matcher m = p.matcher(mobiles);
        return m.matches();
    }

    @Transactional
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

    @Transactional
    @Override
    public User updateOne(User user) {
        //将数据库的修改和es的转为原子操作
        RLock lock = redissonClient.getLock(Constants.USER_LOCK_NAME + user.getId().toString());
        //阻塞式等待，默认为30s过期时间，业务过长会自动续期，加锁业务执行完（通过线程判断）不会自动续期，30s后过期
        lock.lock();
        User ret = null;
        try {
            if (!this.updateById(user)) return null;
            ret = userMapper.selectById(user.getId());
            esService.docInsert(ret, ret.getId().toString(), Constants.USER_ES_INDEX_NAME);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            lock.unlock();
        }
        return ret;
    }
}
