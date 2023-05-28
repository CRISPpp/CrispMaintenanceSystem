package cn.crisp.crispmaintenanceorder.handler;

import cn.crisp.crispmaintenanceorder.security.service.TokenService;
import cn.crisp.exception.BusinessException;
import cn.crisp.exception.NotLoginException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings({"all"})
/**
 * 检验是否登录，如果没有登录，拦截请求
 */
@Component
public class TokenInterceptor implements HandlerInterceptor {
    @Autowired
    private TokenService tokenService;

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) throws Exception {
        if (tokenService.getLoginUser(request) == null) {
            throw new NotLoginException();
        }
        return true;
    }
}