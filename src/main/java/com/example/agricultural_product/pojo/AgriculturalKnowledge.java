package com.example.agricultural_product.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_agricultural_knowledge")
public class AgriculturalKnowledge {
    @TableId(value = "knowledge_id", type = IdType.AUTO)
    private Integer knowledgeId;

    @TableField("title")
    private String title;

    @TableField("content")
    private String content;

    @TableField("expert_id")
    private Long expertId;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}