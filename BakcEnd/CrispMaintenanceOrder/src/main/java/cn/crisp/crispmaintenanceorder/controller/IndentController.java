package cn.crisp.crispmaintenanceorder.controller;

import cn.crisp.common.R;
import cn.crisp.crispmaintenanceorder.security.service.TokenService;
import cn.crisp.crispmaintenanceorder.utils.RedisCache;
import cn.crisp.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/order")
public class IndentController {
    @Autowired
    TokenService tokenService;

    @Autowired
    RedisCache redisCache;

    @GetMapping("/test")
    public R<User> test(HttpServletRequest request) {
        System.out.println((Object) redisCache.getCacheObject("login_tokens:931f1844-d0f8-4a65-bd3e-0ea6eb1d2902"));
        return R.success(tokenService.getLoginUser(request).getUser());
    }
}
