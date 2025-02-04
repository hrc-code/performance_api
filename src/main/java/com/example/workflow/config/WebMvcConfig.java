package com.example.workflow.config;

import com.example.workflow.interceptor.TokenCheckInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

@Slf4j
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Resource
    private TokenCheckInterceptor tokenCheckInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tokenCheckInterceptor)
                // 指定拦截的路径模式
                .addPathPatterns("/**")
                // 排除的路径
                .excludePathPatterns("/login", "/getVerifyCode", "/logout", "/login/test");
    }

}
