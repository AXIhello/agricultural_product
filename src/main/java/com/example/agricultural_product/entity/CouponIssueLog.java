package com.example.agricultural_product.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 优惠券发放记录实体类
 */
@Data
@TableName("tb_coupon_issue_log")
public class CouponIssueLog {
    
    @TableId(value = "log_id", type = IdType.AUTO)
    private Long logId;
    
    private Integer templateId;
    
    private Long userId;
    
    private Long userCouponId;
    
    /**
     * 发放类型：manual/batch/auto/activity
     */
    private String issueType;
    
    /**
     * 发放状态：success/failed
     */
    private String issueStatus;
    
    /**
     * 失败原因
     */
    private String failReason;
    
    /**
     * 操作人ID
     */
    private Long operatorId;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
