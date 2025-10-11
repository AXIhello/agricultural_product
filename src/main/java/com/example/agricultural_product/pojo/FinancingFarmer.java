package com.example.agricultural_product.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_financing_farmers")
public class FinancingFarmer {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("financing_id")
    private Integer financingId;

    @TableField("farmer_id")
    private Long farmerId;

    @TableField("role_in_financing")
    private String roleInFinancing;

    @TableField("create_time")
    private LocalDateTime createTime;
}