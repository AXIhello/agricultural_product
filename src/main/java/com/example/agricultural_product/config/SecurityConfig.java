package com.example.agricultural_product.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> {}) // 跨域配置
                .csrf(csrf -> csrf.disable()) // 禁用 CSRF
                .authorizeHttpRequests(auth -> auth
                        // 登录注册接口允许所有访问
                        .requestMatchers("/api/user/login", "/api/user/register").permitAll()
                        // 邮箱验证相关接口允许所有访问
                        .requestMatchers("/api/email/**").permitAll()
                        // 其他所有 /api/** 接口需要鉴权
                        .requestMatchers("/api/**").authenticated()
                        // 非 /api/ 的接口允许访问
                        .anyRequest().permitAll()
                )
                // 在 UsernamePasswordAuthenticationFilter 之前添加 JWT 过滤器
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
