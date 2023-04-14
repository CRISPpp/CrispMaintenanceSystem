package cn.crisp.crispmaintenanceuser.service.impl;

import cn.crisp.common.R;
import cn.crisp.crispmaintenanceuser.dto.LoginDto;
import cn.crisp.crispmaintenanceuser.entity.User;
import cn.crisp.crispmaintenanceuser.mapper.UserMapper;
import cn.crisp.crispmaintenanceuser.service.UserService;
import cn.crisp.crispmaintenanceuser.utils.RedisCache;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService{
    @Autowired
    RedisCache redisCache;

    @Autowired
    UserMapper userMapper;

    @Override
    public R<String> register(LoginDto loginDto) {
        if (loginDto.getPhone() == null || loginDto.getPhone().length() == 0 || loginDto.getPassword().length() == 0 || loginDto.getPassword() == null){
            return R.error("参数错误");
        }
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, loginDto.getPhone());
        if (userMapper.selectOne(wrapper) != null) return R.error("手机号已被注册");
        User user = new User();
        user.setId(null);
        user.setPhone(loginDto.getPhone());
        user.setPassword(loginDto.getPassword());
        user.setUsername(loginDto.getPhone());
        this.save(user);

        return R.success("添加成功");
    }
}
