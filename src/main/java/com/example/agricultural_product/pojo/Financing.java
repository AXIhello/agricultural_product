package com.example.agricultural_product.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("tb_financing")
public class Financing {
    @TableId(value = "financing_id", type = IdType.AUTO)
    private Integer financingId;

    @TableField("initiating_farmer_id")
    private Long initiatingFarmerId;

    @TableField("amount")
    private BigDecimal amount;

    @TableField("purpose")
    private String purpose;

    @TableField("application_status")
    private String applicationStatus;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    // 非数据库字段：参与农户列表（用于返回详情）
    @TableField(exist = false)
    private List<FinancingFarmer> farmers;
}