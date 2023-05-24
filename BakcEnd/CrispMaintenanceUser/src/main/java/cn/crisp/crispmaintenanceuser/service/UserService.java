package cn.crisp.crispmaintenanceuser.service;

import cn.crisp.common.R;

import cn.crisp.dto.LoginDto;
import cn.crisp.dto.MailUpdateDto;
import cn.crisp.dto.PasswordUpdateDto;
import cn.crisp.entity.Address;
import cn.crisp.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface UserService extends IService<User> {
    R<String> register(LoginDto loginDto);
    User selectByPhone(String phone);

    R<User> getByToken(HttpServletRequest request);

    User selectById(Long id);

    R<User> updateOne(HttpServletRequest request, User user);

    R<User> updatePhone(HttpServletRequest request, User user);

    R<User> updateMail(HttpServletRequest request, MailUpdateDto mailUpdateDto);

    R<User> updatePassword(HttpServletRequest request, PasswordUpdateDto passwordUpdateDto);
}
