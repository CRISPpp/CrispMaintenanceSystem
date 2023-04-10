package cn.crisp.crispmaintenanceuser.controller;

import cn.crisp.common.R;
import cn.crisp.crispmaintenanceuser.dto.LoginDto;
import cn.crisp.crispmaintenanceuser.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    UserService userService;

    @GetMapping("/test")
    public R<String> test(){
        return R.success("1111");
    }
}
