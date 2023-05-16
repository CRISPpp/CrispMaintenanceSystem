package cn.crisp.crispmaintenanceuser.security.service;


import cn.crisp.crispmaintenanceuser.entity.LoginUser;
import cn.crisp.crispmaintenanceuser.service.UserService;
import cn.crisp.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 用户验证处理
 */

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private static final Logger log = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    @Autowired
    private UserService userService;


    public UserDetails createLoginUser(User user){
        return new LoginUser(user);
    }

    @Override
    public UserDetails loadUserByUsername(String phone) throws UsernameNotFoundException {
        User user = userService.selectByPhone(phone);
        if(user == null){
            log.info("登录用户：{} 不存在", phone);
            throw new RuntimeException("登录用户：" + phone + " 不存在");
        }
        return createLoginUser(user);
    }
}
