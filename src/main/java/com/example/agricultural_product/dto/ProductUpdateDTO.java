package com.example.agricultural_product.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
public class ProductUpdateDTO {

    // 更新时需要知道要更新哪个产品
    private Integer productId;

    // 以下字段为可更新字段
    private String productName; // 产品名称

    private String description; // 产品描述

    private BigDecimal price; // 价格

    private Integer stock; // 库存

    private String status; // 状态，允许上架/下架 (active/inactive)

    private String prodCat; // 产品大类

    private String prodPcat; // 产品小类

    private String unitInfo; // 产品单位

    private String specInfo; // 产品信息

    private String place; // 产地

    // imagePath (如果有图片更新，处理逻辑会更复杂，通常会有一个单独的图片更新接口或直接在 DTO 中包含新的路径/文件)
}