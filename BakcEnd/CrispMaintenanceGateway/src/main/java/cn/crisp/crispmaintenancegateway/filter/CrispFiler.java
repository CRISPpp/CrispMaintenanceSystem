package cn.crisp.crispmaintenancegateway.filter;

import com.alibaba.fastjson2.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * @author crisp
 * 用来过滤没有认证的服务
 */
@Component
public class CrispFiler implements GlobalFilter, Ordered {
    private AntPathMatcher antPathMatcher = new AntPathMatcher();

    // 令牌自定义标识
    @Value("${token.header}")
    private String header;

    private Mono<Void> handler(ServerHttpResponse response) {
        JSONObject ret = new JSONObject();
        ret.put("code", "1");
        ret.put("msg", "没有权限，请登录");
        byte[] bits = ret.toString().getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = (DataBuffer) response.bufferFactory().wrap(bits);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        if (!antPathMatcher.match("/user/register", path) && !antPathMatcher.match("/user/login", path)) {
            if (!request.getHeaders().containsKey(header)) return handler(exchange.getResponse());
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
