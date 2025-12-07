package com.example.agricultural_product.dto.coupon;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券响应DTO
 */
@Data
public class CouponDTO {
    
    private Long userCouponId;
    
    private Integer templateId;
    
    private String couponCode;
    
    private String couponName;
    
    /**
     * 优惠券类型：full_reduction/discount/fixed_amount/free_shipping
     */
    private String couponType;
    
    /**
     * 优惠券类型中文
     */
    private String couponTypeText;
    
    /**
     * 优惠值
     */
    private BigDecimal discountValue;
    
    /**
     * 最低使用金额
     */
    private BigDecimal minOrderAmount;
    
    /**
     * 状态：unused/used/expired/locked
     */
    private String status;
    
    /**
     * 状态中文
     */
    private String statusText;
    
    /**
     * 领取时间
     */
    private LocalDateTime receiveTime;
    
    /**
     * 有效期开始
     */
    private LocalDateTime validStart;
    
    /**
     * 有效期结束
     */
    private LocalDateTime validEnd;
    
    /**
     * 使用时间
     */
    private LocalDateTime usedTime;
    
    /**
     * 实际优惠金额
     */
    private BigDecimal discountAmount;
    
    /**
     * 优惠券描述
     */
    private String description;
    
    /**
     * 是否即将过期（3天内）
     */
    private Boolean expiringSoon;
    
    /**
     * 是否可用
     */
    private Boolean available;
}
