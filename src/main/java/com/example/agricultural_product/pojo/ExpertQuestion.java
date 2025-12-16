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

	@TableField("accepted_answer_id")
	private Integer acceptedAnswerId; // 可为空

	@TableField("create_time")
	private LocalDateTime createTime;

	@TableField("update_time")
	private LocalDateTime updateTime;
    
	/**
     * 提问者名字 
     */
    @TableField(exist = false) 
    private String farmerName;

    public String getFarmerName() {
        return farmerName;
    }

    public void setFarmerName(String farmerName) {
        this.farmerName = farmerName;
    }

} 