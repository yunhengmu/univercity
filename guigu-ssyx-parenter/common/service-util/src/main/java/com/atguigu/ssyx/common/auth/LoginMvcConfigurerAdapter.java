package com.atguigu.ssyx.common.auth;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


import javax.annotation.Resource;


@Configuration
public class LoginMvcConfigurerAdapter implements WebMvcConfigurer {
    @Resource
    private RedisTemplate redisTemplate;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(new UserLoginInterceptor(redisTemplate))
                // 拦截的请求
                .addPathPatterns("/api/**")
                // 不用拦截的请求
                .excludePathPatterns("/api/user/weixin/wxLogin/*");
    }
}