package com.example.agricultural_product.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("users") // 对应数据库表名
public class User {

    @TableId(value = "user_id", type = IdType.AUTO) // 自增主键
    private Long userId;

    @TableField("user_name") // 登录账号
    private String userName;

    @TableField("password") // 加密后的密码
    private String password;

    @TableField("name") // 昵称
    private String name;

    @TableField("email") // 邮箱
    private String email;

    @TableField("role") // 用户角色
    private String role;

    @TableField("region") // 用户所在地区
    private String region;

    @TableField("credit_score") // 用户信用分
    private Integer creditScore;

    @TableField("historical_success_rate") // 历史融资成功率（百分比）
    private Double historicalSuccessRate;

    @TableField("average_financing_amount") // 平均融资金额
    private Double averageFinancingAmount;

    @TableField("financing_activity_level") // 融资活跃度（low/medium/high）
    private String financingActivityLevel;
}
