package cn.crisp.crispmaintenanceuser.service;

import cn.crisp.common.R;

import cn.crisp.dto.LoginDto;
import cn.crisp.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;

public interface UserService extends IService<User> {
    public R<String> register(LoginDto loginDto);
    public User selectByPhone(String phone);

    public R<User> getByToken(HttpServletRequest request);

    public User selectById(Long id);

    public User updateOne(User user);
}
