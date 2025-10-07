package com.example.agricultural_product.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tb_cart_items")
public class CartItem {
	@TableId(value = "item_id", type = IdType.AUTO)
	private Integer itemId;

	@TableField("cart_id")
	private Integer cartId;

	@TableField("product_id")
	private Integer productId;

	@TableField("quantity")
	private Integer quantity;

	@TableField("price_at_add")
	private BigDecimal priceAtAdd;

	@TableField("add_time")
	private LocalDateTime addTime;
}
