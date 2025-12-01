package com.example.agricultural_product.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_order_item_review")
public class OrderItemReview {
    @TableId(value = "review_id", type = IdType.AUTO)
    private Long reviewId;

    @TableField("item_id")
    private Integer itemId;

    @TableField("order_id")
    private Integer orderId;

    @TableField("product_id")
    private Integer productId;

    @TableField("user_id")
    private Long userId;

    @TableField("rating")
    private Integer rating; // 1-5 分

    @TableField("content")
    private String content;

    @TableField("is_anonymous")
    private Integer isAnonymous; // 0-否,1-是

    @TableField("status")
    private String status; // published / deleted / hidden

    @TableField("append_content")
    private String appendContent;

    @TableField("append_time")
    private LocalDateTime appendTime;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
