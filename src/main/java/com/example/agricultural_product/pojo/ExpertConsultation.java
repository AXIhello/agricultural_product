package com.example.agricultural_product.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_expert_consultation")
public class ExpertConsultation {
    @TableId(value = "consultation_id", type = IdType.AUTO)
    private Integer consultationId;

    @TableField("farmer_id")
    private Long farmerId;

    @TableField("expert_id")
    private Long expertId;

    @TableField("slot_id")
    private Integer slotId; // 新增字段，指向工作时间段

    @TableField("consultation_time")
    private LocalDateTime consultationTime;

    @TableField("status")
    private String status; // scheduled/completed/cancelled

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String expertName; 
}