package com.example.agricultural_product.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_expert_questions")
public class ExpertQuestion {
	@TableId(value = "question_id", type = IdType.AUTO)
	private Integer questionId;

	@TableField("farmer_id")
	private Long farmerId;

	@TableField("title")
	private String title;

	@TableField("content")
	private String content;

	@TableField("status")
	private String status; // open / answered

	@TableField("answer_expert_id")
	private Long answerExpertId; // 可为空

	@TableField("answer_content")
	private String answerContent; // 可为空

	@TableField("answer_time")
	private LocalDateTime answerTime; // 可为空

	@TableField("create_time")
	private LocalDateTime createTime;

	@TableField("update_time")
	private LocalDateTime updateTime;
} 