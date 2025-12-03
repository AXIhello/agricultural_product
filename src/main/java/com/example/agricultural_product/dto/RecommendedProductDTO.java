package com.example.agricultural_product.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 推荐商品DTO
 * 基于评价统计字段的商品推荐
 */
@Data
public class RecommendedProductDTO {
    
    /**
     * 商品ID
     */
    private Integer productId;
    
    /**
     * 商品名称
     */
    private String productName;
    
    /**
     * 商品描述
     */
    private String description;
    
    /**
     * 价格
     */
    private BigDecimal price;
    
    /**
     * 库存
     */
    private Integer stock;
    
    /**
     * 农户ID
     */
    private Long farmerId;
    
    /**
     * 商品状态
     */
    private String status;
    
    /**
     * 图片路径
     */
    private String imagePath;
    
    /**
     * 商品大类
     */
    private String prodCat;
    
    /**
     * 商品小类
     */
    private String prodPcat;
    
    /**
     * 产地
     */
    private String place;
    
    /**
     * 平均评分 (0.0-5.0)
     */
    private Double averageRating;
    
    /**
     * 评价数量
     */
    private Integer ratingCount;
    
    /**
     * 推荐分数（用于排序）
     */
    private Double recommendScore;
    
    /**
     * 推荐理由
     */
    private String recommendReason;
    
    /**
     * 热度分数（基于评价数量）
     */
    private Double popularityScore;
    
    /**
     * 质量分数（基于平均评分）
     */
    private Double qualityScore;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 是否有库存
     */
    private Boolean hasStock;
    
    /**
     * 评价等级描述
     */
    private String ratingLevel;
}