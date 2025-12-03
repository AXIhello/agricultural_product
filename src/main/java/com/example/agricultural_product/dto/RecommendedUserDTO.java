package com.example.agricultural_product.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 推荐联合融资用户DTO
 */
@Data
public class RecommendedUserDTO {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String userName;
    
    /**
     * 昵称
     */
    private String name;
    
    /**
     * 所在地区
     */
    private String region;
    
    /**
     * 信用分
     */
    private Integer creditScore;
    
    /**
     * 历史融资成功率（百分比）
     */
    private BigDecimal historicalSuccessRate;
    
    /**
     * 平均融资金额
     */
    private BigDecimal averageFinancingAmount;
    
    /**
     * 融资活跃度（low/medium/high）
     */
    private String financingActivityLevel;
    
    /**
     * 是否为同地区用户
     */
    private Boolean sameRegion;
    
    /**
     * 推荐分数（用于排序）
     */
    private Double recommendScore;
}