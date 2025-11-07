package com.example.agricultural_product.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tb_product")
public class Product {
    @TableId(value = "product_id", type = IdType.AUTO)
    private Integer productId;

    @TableField("product_name")
    private String productName;

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
    private String prodPcat;

    @TableField("unitInfo")
     private String unitInfo;
}