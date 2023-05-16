package cn.crisp.crispmaintenanceuser.service;

import cn.crisp.common.R;

import cn.crisp.dto.LoginDto;
import cn.crisp.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

public interface UserService extends IService<User> {
    public R<String> register(LoginDto loginDto);
    public User selectByPhone(String phone);
}
