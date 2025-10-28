package com.example.agricultural_product.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@TableName("tb_expert_working_slots")
public class ExpertWorkingSlot {
    @TableId(value = "slot_id", type = IdType.AUTO)
    private Integer slotId;

    @TableField("expert_id")
    private Long expertId;

    @TableField("work_date")
    private LocalDate workDate;

    @TableField("start_time")
    private LocalTime startTime;

    @TableField("end_time")
    private LocalTime endTime;

    @TableField("capacity")
    private Integer capacity;

    @TableField("booked_count")
    private Integer bookedCount;

    @TableField("status")
    private String status; // open/closed

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}