package com.example.agricultural_product.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("tb_product")
public class Product {
    @TableId(value = "product_id", type = IdType.AUTO)
    private Integer productId;

    @TableField("product_name")
    private String productName; //农产品的名字（预测要用）

    @TableField("description")
    private String description;

    @TableField("price")
    private BigDecimal price;

    @TableField("stock")
    private Integer stock;

    @TableField("farmer_id")
    private Long farmerId;

    @TableField("status")
    private String status;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @TableField("image_path")
    private String imagePath;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("prodCat")
    private String prodCat;

    @TableField("prodPcat")
    private String prodPcat;    //农产品小类（预测要用）

    @TableField("unitInfo")
    private String unitInfo;

    @TableField("specInfo")
    private String specInfo;    //农产品信息（预测要用）

    @TableField("place")
    private String place;

    @TableField(exist = false) // 关键：标记该字段不在 tb_product 表中，不参与SQL
    private LocalDate forecastDate; // 预测日期（如：2025-12-31，仅用于返回前端）

    @TableField(exist = false) // 关键：标记该字段不在 tb_product 表中，不参与SQL
    private BigDecimal predictedPrice; // 预测价格（模型输出，仅用于返回前端）

    @TableField(exist = false)
    private String labelPre; // 关键：判断分支的字段


}