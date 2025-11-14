package com.example.agricultural_product.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_auto_reply_rules")
public class AutoReplyRule {
    @TableId(value = "rule_id", type = IdType.AUTO)
    private Long ruleId;

    @TableField("seller_id")
    private Long sellerId;

    @TableField("keyword")
    private String keyword;

    @TableField("match_type")
    private String matchType; // contains/exact/regex

    @TableField("reply_text")
    private String replyText;

    @TableField("enabled")
    private Boolean enabled;

    @TableField("priority")
    private Integer priority;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}