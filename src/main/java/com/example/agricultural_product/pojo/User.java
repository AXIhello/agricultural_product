package com.example.agricultural_product.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("users") // 数据库表名
public class User {

    @TableId(value = "user_id", type = IdType.AUTO) // 自增主键
    private Long userId;

    @TableField("user_name") // 账号（登录用）
    private String userName;

    @TableField("password")
    private String password;

    @TableField("name") // 昵称
    private String name;

    @TableField("email")
    private String email;

    @TableField("role")
    private String role;

}
