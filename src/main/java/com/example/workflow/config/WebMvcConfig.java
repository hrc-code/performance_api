package com.example.workflow.config;

import com.example.workflow.interceptor.TokenCheckInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Slf4j
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    //@JsonFormat(shape = JsonFormat.Shape.STRING)
    // public void extendMessageConverters(List<HttpMessageConverter<?>> converters){
    //     log.info("拓展消息转换器");
    //     MappingJackson2CborHttpMessageConverter messageConverter=new MappingJackson2CborHttpMessageConverter();
    //     messageConverter.setObjectMapper(new JacksonObjectMapper());
    //     converters.add(0,messageConverter);
    // }
    @Autowired
    private TokenCheckInterceptor tokenCheckInterceptor;
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tokenCheckInterceptor)
                .addPathPatterns("/**") // 指定拦截的路径模式
                .excludePathPatterns("/login","/getVerifyCode"); // 排除的路径
    }
}
