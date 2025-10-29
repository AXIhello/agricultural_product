package com.example.agricultural_product.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tb_bank_products")
public class BankProduct {
    @TableId(value = "product_id", type = IdType.AUTO)
    private Integer productId;

    @TableField("bank_user_id")
    private Long bankUserId;

    @TableField("product_name")
    private String productName;

    @TableField("description")
    private String description;

    @TableField("term_months")
    private Integer termMonths;

    @TableField("interest_rate")
    private BigDecimal interestRate;

    @TableField("min_amount")
    private BigDecimal minAmount;

    @TableField("max_amount")
    private BigDecimal maxAmount;

    @TableField("status")
    private String status; // active/inactive

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}