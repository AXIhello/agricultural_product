package com.example.agricultural_product.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tb_expert_profiles")
public class ExpertProfile {
	@TableId("expert_id")
	private Long expertId;

	@TableField("specialization")
	private String specialization;

	@TableField("bio")
	private String bio;

	@TableField("photo_url")
	private String photoUrl;

	@TableField("average_rating")
	private BigDecimal averageRating;

	@TableField("consultation_fee")
	private BigDecimal consultationFee;

	@TableField("create_time")
	private LocalDateTime createTime;

	@TableField("update_time")
	private LocalDateTime updateTime;
} 