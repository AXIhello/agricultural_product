package com.example.agricultural_product.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tb_order")
public class Order {
	@TableId(value = "order_id", type = IdType.AUTO)
	private Integer orderId;

	@TableField("user_id")
	private Long userId;

	@TableField("order_date")
	private LocalDateTime orderDate;

	@TableField("total_amount")
	private BigDecimal totalAmount;

	@TableField("status")
	private String status;

	@TableField("shipping_address_id")
	private Integer shippingAddressId;

	@TableField("create_time")
	private LocalDateTime createTime;

	@TableField("update_time")
	private LocalDateTime updateTime;

	@TableField(exist = false) 
	private Address address;
}
