package com.example.agricultural_product.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户优惠券实体类
 */
@Data
@TableName("tb_user_coupon")
public class UserCoupon {
    
    @TableId(value = "user_coupon_id", type = IdType.AUTO)
    private Long userCouponId;
    
    private Long userId;
    
    private Integer templateId;
    
    /**
     * 优惠券券码（唯一）
     */
    private String couponCode;
    
    /**
     * 优惠券名称（冗余）
     */
    private String couponName;
    
    /**
     * 优惠券类型
     */
    private String couponType;
    
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
     * 使用的订单ID
     */
    private Integer orderId;
    
    /**
     * 实际优惠金额
     */
    private BigDecimal discountAmount;
    
    /**
     * 获取方式：manual/auto/activity/share
     */
    private String acquireType;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
