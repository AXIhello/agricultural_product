package com.example.agricultural_product.dto;

import lombok.Data;

@Data
public class UserApplicationDTO {
    private String userName;        // 用户名
    private String password;        // 密码
    private String name;           // 真实姓名
    private String email;          // 邮箱
    private String phone;          // 电话
    private String applyRole;      // 申请角色(expert/bank)
    private String description;    // 申请说明
    private String reason;         // 申请原因
}