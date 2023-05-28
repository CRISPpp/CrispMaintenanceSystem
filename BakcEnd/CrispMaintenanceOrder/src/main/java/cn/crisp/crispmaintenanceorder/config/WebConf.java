package cn.crisp.crispmaintenanceorder.config;


import cn.crisp.common.JacksonObjectMapper;
import cn.crisp.crispmaintenanceorder.handler.TokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.List;

@Configuration
public class WebConf extends WebMvcConfigurationSupport {
    @Autowired
    private TokenInterceptor tokenInterceptor;

    /**
     * 设置静态资源映射
     */
    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        //下面是swagger的配置
        registry.addResourceHandler("doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
        registry.addResourceHandler("/**").addResourceLocations("classpath:/templates/");
    }

    /**
     * 将json序列化和反序列化
     */
    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters){
        //消息转换器,将service返回的数据转换成json
        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
        //设置对象转换器，底层使用Jackson将java对象转为json
        messageConverter.setObjectMapper(new JacksonObjectMapper());
        //追加到转换器容器中，index为0表示优先级最高
        converters.add(0, messageConverter);
    }

    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        //检验是否登录，如果没有登录，拦截请求
        registry.addInterceptor(tokenInterceptor).addPathPatterns("/**");
    }

    /**
     * 密码明文加密方式配置
     * @return
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
