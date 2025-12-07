package com.example.agricultural_product.dto.coupon;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 创建优惠券模板DTO
 */
@Data
public class CreateCouponTemplateDTO {
    
    @NotBlank(message = "优惠券名称不能为空")
    private String templateName;
    
    @NotBlank(message = "优惠券类型不能为空")
    private String couponType;
    
    @NotNull(message = "优惠值不能为空")
    @DecimalMin(value = "0.01", message = "优惠值必须大于0")
    private BigDecimal discountValue;
    
    @DecimalMin(value = "0", message = "最低使用金额不能为负数")
    private BigDecimal minOrderAmount = BigDecimal.ZERO;
    
    private BigDecimal maxDiscountAmount;
    
    @NotBlank(message = "适用范围不能为空")
    private String applicableScope;
    
    private String applicableValues;
    
    @NotNull(message = "发行总量不能为空")
    private Integer totalQuantity;
    
    @NotNull(message = "每人限领数量不能为空")
    private Integer perUserLimit;
    
    @NotBlank(message = "有效期类型不能为空")
    private String validityType;
    
    private LocalDateTime validityStart;
    
    private LocalDateTime validityEnd;
    
    private Integer validityDays;
    
    private String description;
}
