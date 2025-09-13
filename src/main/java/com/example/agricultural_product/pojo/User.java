package com.example.agricultural_product.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Data
@TableName("users") // 数据库表名
public class User {

    @TableId // 主键（账号）
    private String user_id;

    private String password;

    private String user_name; // 昵称

    private String email;

    private String role;

    /**
     * 设置密码时自动加密
     */
    public void setPassword(String password) {
        if (password != null && !password.isEmpty()) {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            this.password = encoder.encode(password);
        }
    }
}
