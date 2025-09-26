package com.example.agricultural_product.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_expert_answers")
public class ExpertAnswer {
	@TableId(value = "answer_id", type = IdType.AUTO)
	private Integer answerId;

	@TableField("question_id")
	private Integer questionId;

	@TableField("responder_id")
	private Long responderId;

	@TableField("content")
	private String content;

	@TableField("is_accepted")
	private Integer isAccepted; // 0/1

	@TableField("create_time")
	private LocalDateTime createTime;

	@TableField("update_time")
	private LocalDateTime updateTime;
} 