package com.example.agricultural_product.config;

import com.example.agricultural_product.mapper.UserMapper;
import com.example.agricultural_product.pojo.User;
import jakarta.annotation.Resource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Resource
    private UserMapper userMapper;

    @Override
    public void run(String... args) {
        // 1. 自动建表（如果不存在）
        String createTableSql = """
            CREATE TABLE IF NOT EXISTS users (
                user_id VARCHAR(50) PRIMARY KEY COMMENT '账号，主键',
                password VARCHAR(255) NOT NULL COMMENT '加密后的密码',
                user_name VARCHAR(100) UNIQUE COMMENT '用户昵称，唯一',
                email VARCHAR(100),
                role VARCHAR(50) NOT NULL COMMENT '角色'
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表'
            """;

        jdbcTemplate.execute(createTableSql);

        // 2. 检查是否已有 admin 账号
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE user_id = 'admin'",
                Long.class
        );

        if (count != null && count == 0) {
            // 插入默认管理员
            User user = new User();
            user.setUser_id("admin");      // 登录账号
            user.setPassword("admin123"); // 自动加密
            user.setUser_name("管理员");    // 昵称
            user.setRole("admin");

            userMapper.insert(user);
            System.out.println("初始化管理员成功: admin / admin123");
        }
    }
}
