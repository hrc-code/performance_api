package com.example.workflow.config;

//import com.performance.common.JacksonObjectMapper;
import com.example.workflow.common.JacksonObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.cbor.MappingJackson2CborHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

import java.util.List;

@Slf4j
@Configuration
public class WebMvcConfig {
    protected void addResourceHandlers(ResourceHandlerRegistry registry){
        registry.addResourceHandler("/backend/**").addResourceLocations("classpath:/backend/");
        registry.addResourceHandler("/front/**").addResourceLocations("classpath:/front/");
    }
    //@JsonFormat(shape = JsonFormat.Shape.STRING)
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters){
        log.info("拓展消息转换器");
        MappingJackson2CborHttpMessageConverter messageConverter=new MappingJackson2CborHttpMessageConverter();
        messageConverter.setObjectMapper(new JacksonObjectMapper());
        converters.add(0,messageConverter);
    }
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
}
