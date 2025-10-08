package com.example.agricultural_product.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("tb_purchase_demands")
public class PurchaseDemand {
	@TableId(value = "demand_id", type = IdType.AUTO)
	private Integer demandId;

	@TableField("buyer_id")
	private Long buyerId;

	@TableField("product_name_desired")
	private String productNameDesired;

	@TableField("quantity_desired")
	private BigDecimal quantityDesired;

	@TableField("max_price_per_unit")
	private BigDecimal maxPricePerUnit;

	@TableField("details")
	private String details;

	@TableField("delivery_date_desired")
	private LocalDate deliveryDateDesired;

	@TableField("status")
	private String status; // open / matched / closed / expired

	@TableField("create_time")
	private LocalDateTime createTime;

	@TableField("update_time")
	private LocalDateTime updateTime;
} 