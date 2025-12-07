package com.example.agricultural_product.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券模板实体类
 */
@Data
@TableName("tb_coupon_template")
public class CouponTemplate {
    
    @TableId(value = "template_id", type = IdType.AUTO)
    private Integer templateId;
    
    private String templateName;
    
    /**
     * 优惠券类型：full_reduction(满减)/discount(折扣)/fixed_amount(固定金额)/free_shipping(免运费)
     */
    private String couponType;
    
    /**
     * 优惠值（满减金额/折扣率/固定金额等）
     */
    private BigDecimal discountValue;
    
    /**
     * 最低使用金额
     */
    private BigDecimal minOrderAmount;
    
    /**
     * 最大优惠金额（折扣券用）
     */
    private BigDecimal maxDiscountAmount;
    
    /**
     * 适用范围：all/category/product/farmer
     */
    private String applicableScope;
    
    /**
     * 适用范围的值（JSON格式）
     */
    private String applicableValues;
    
    /**
     * 发行总量（-1表示无限制）
     */
    private Integer totalQuantity;
    
    /**
     * 已发放数量
     */
    private Integer issuedQuantity;
    
    /**
     * 每人限领数量
     */
    private Integer perUserLimit;
    
    /**
     * 有效期类型：fixed(固定)/relative(相对)
     */
    private String validityType;
    
    /**
     * 固定有效期开始时间
     */
    private LocalDateTime validityStart;
    
    /**
     * 固定有效期结束时间
     */
    private LocalDateTime validityEnd;
    
    /**
     * 相对有效天数
     */
    private Integer validityDays;
    
    /**
     * 状态：draft/active/inactive/expired
     */
    private String status;
    
    /**
     * 优惠券使用说明
     */
    private String description;
    
    /**
     * 发放者ID
     */
    private Long issuerId;
    
    /**
     * 发放者类型：admin/farmer
     */
    private String issuerType;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
