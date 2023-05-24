package cn.crisp.crispmaintenanceuser.service;

import cn.crisp.common.R;

import cn.crisp.dto.LoginDto;
import cn.crisp.dto.MailUpdateDto;
import cn.crisp.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;

public interface UserService extends IService<User> {
    public R<String> register(LoginDto loginDto);
    public User selectByPhone(String phone);

    public R<User> getByToken(HttpServletRequest request);

    public User selectById(Long id);

    public R<User> updateOne(HttpServletRequest request,User user);

    public R<User> updatePhone(HttpServletRequest request, User user);

    public R<User> updateMail(HttpServletRequest request, MailUpdateDto mailUpdateDto);
}
