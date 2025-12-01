// 文件路径: com/example/agricultural_product/pojo/OrderItem.java

package com.example.agricultural_product.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tb_order_item")
public class OrderItem {
    @TableId(value = "item_id", type = IdType.AUTO)
    private Integer itemId;

    @TableField("order_id")
    private Integer orderId;

    @TableField("product_id")
    private Integer productId;

    @TableField("quantity")
    private Integer quantity;

    @TableField("unit_price")
    private BigDecimal unitPrice;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("is_reviewed")
    private Integer isReviewed;

    @TableField("status")
    private String status; // PENDING, PAID, CANCELLED, SHIPPED, RECEIVED, REVIEWED, REFUND_*

    @TableField("refund_status")
    private String refundStatus; // NULL, REQUESTED, APPROVED, REJECTED, COMPLETED

    @TableField("refund_reason")
    private String refundReason;

    @TableField(exist = false)
    private String productName;

    @TableField(exist = false) 
    private Long farmerId;
}