package com.example.agricultural_product.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 优惠券活动实体类
 */
@Data
@TableName("tb_coupon_activity")
public class CouponActivity {
    
    @TableId(value = "activity_id", type = IdType.AUTO)
    private Integer activityId;
    
    private String activityName;
    
    /**
     * 活动类型：new_user/order_complete/purchase_amount/share/sign_in/birthday
     */
    private String activityType;
    
    /**
     * 触发条件（JSON格式）
     */
    private String triggerCondition;
    
    /**
     * 优惠券模板ID列表（JSON格式）
     */
    private String templateIds;
    
    /**
     * 活动开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 活动结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 状态：pending/active/ended/cancelled
     */
    private String status;
    
    /**
     * 优先级
     */
    private Integer priority;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
