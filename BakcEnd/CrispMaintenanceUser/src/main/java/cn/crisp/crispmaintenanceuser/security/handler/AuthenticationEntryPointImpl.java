package cn.crisp.crispmaintenanceuser.security.handler;

import cn.crisp.common.R;
import cn.crisp.crispmaintenanceuser.utils.ServletUtils;
import com.alibaba.fastjson.JSON;

import org.apache.http.HttpStatus;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;

@Component
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint, Serializable {
    private static final long serialVersionUID = 12344663454L;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, org.springframework.security.core.AuthenticationException authException) throws IOException, ServletException {
        int code = HttpStatus.SC_UNAUTHORIZED;
        String msg = String.format("请求访问：{}，认证失败，无法访问系统资源", request.getRequestURI());
        ServletUtils.renderString(response, JSON.toJSONString(R.errorWithCode(code, msg)));
    }
}
