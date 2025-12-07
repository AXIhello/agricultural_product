package com.example.agricultural_product.dto.coupon;

import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 批量发放优惠券DTO
 */
@Data
public class BatchIssueCouponDTO {
    
    @NotNull(message = "优惠券模板ID不能为空")
    private Integer templateId;
    
    @NotEmpty(message = "用户ID列表不能为空")
    private List<Long> userIds;
    
    /**
     * 发放类型：batch批量发放
     */
    private String issueType = "batch";
}
