package com.jira.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
//标记这是配置类
@Configuration
public class PasswordEncoderConfig {
//把方法返回值注册进 Spring 容器，其他地方就能`注入` 使用了
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
