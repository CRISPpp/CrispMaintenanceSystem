package cn.crisp.crispmaintenanceuser.service;

import cn.crisp.common.R;

import cn.crisp.dto.*;
import cn.crisp.entity.Address;
import cn.crisp.entity.EngineerAttribute;
import cn.crisp.entity.User;
import cn.crisp.entity.UserAttribute;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;

public interface UserService extends IService<User> {
    R<String> register(RegisterDto registerDto);

    User selectByPhone(String phone);

    R<User> getByToken(HttpServletRequest request);

    User selectById(Long id);

    R<User> updateOne(HttpServletRequest request, User user);

    R<User> updatePhone(HttpServletRequest request, User user);

    R<User> updateMail(HttpServletRequest request, MailUpdateDto mailUpdateDto);

    R<User> updatePassword(HttpServletRequest request, PasswordUpdateDto passwordUpdateDto);

    R<Boolean> pay(PayDto payDto);

    R<UserAttribute> addUserAttribute(HttpServletRequest request, UserAttribute userAttribute);

    R<EngineerAttribute> addEngineerAttribute(HttpServletRequest request, EngineerAttribute engineerAttribute);

    R<UserAttribute> updateUserAttribute(HttpServletRequest request, UserAttribute userAttribute);

    R<EngineerAttribute> updateEngineerAttribute(HttpServletRequest request, EngineerAttribute engineerAttribute);
}
