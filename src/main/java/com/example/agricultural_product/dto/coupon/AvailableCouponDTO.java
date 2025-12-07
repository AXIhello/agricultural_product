package com.example.agricultural_product.dto.coupon;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 订单可用优惠券查询DTO
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AvailableCouponDTO extends CouponDTO {
    
    /**
     * 该订单可节省金额
     */
    private BigDecimal canSaveAmount;
    
    /**
     * 是否推荐使用（最优惠）
     */
    private Boolean recommended;
    
    /**
     * 不可用原因
     */
    private String unavailableReason;
    
    /**
     * 是否满足使用条件
     */
    private Boolean meetsCondition;
}
