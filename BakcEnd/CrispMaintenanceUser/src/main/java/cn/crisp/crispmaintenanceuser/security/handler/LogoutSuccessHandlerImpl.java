package cn.crisp.crispmaintenanceuser.security.handler;

import cn.crisp.common.R;
import cn.crisp.crispmaintenanceuser.entity.LoginUser;
import cn.crisp.crispmaintenanceuser.security.service.TokenService;
import cn.crisp.crispmaintenanceuser.utils.ServletUtils;
import com.alibaba.fastjson2.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;

import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 自定义的登出处理
 */
@Configuration
public class LogoutSuccessHandlerImpl implements LogoutSuccessHandler {

    @Autowired
    private TokenService tokenService;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        LoginUser loginUser = tokenService.getLoginUser(request);

        if(loginUser != null){
            //删除用户记录，同时去除缓存的记录
            tokenService.delLoginUser(loginUser.getToken());
        }
        ServletUtils.renderString(response, JSON.toJSONString(R.success("退出成功")));
    }
}
