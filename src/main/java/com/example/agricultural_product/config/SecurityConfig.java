package com.example.agricultural_product.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // 关闭 CSRF，前后端分离常用
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/user/login", "/api/user/register").permitAll() // 放行登录和注册
                        .anyRequest().permitAll() // 其他请求也先放行，开发阶段方便调试
                );

        return http.build();
    }
}
