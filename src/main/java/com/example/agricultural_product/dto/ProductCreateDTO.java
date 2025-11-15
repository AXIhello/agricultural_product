package com.example.agricultural_product.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
public class ProductCreateDTO {

    // 注意：farmerId 在 Controller 中从 Token 中获取并设置，DTO 中可以省略，
    // 但在您提供的代码中，Controller 直接使用了 Product 实体作为 @RequestBody/@RequestPart，
    // 故此 DTO 实际上可能未被使用，或者只是作为概念上的划分。
    //
    // 为了与您提供的 Controller/Service 代码逻辑保持一致，这里的字段与 Product 实体高度相似，
    // 仅省略了那些由数据库/后端自动生成的字段。

    private String productName; // 产品名称 (必填)

    private String description; // 产品描述

    private BigDecimal price; // 价格 (必填)

    private Integer stock; // 库存 (默认0)

    private String status; // 产品状态 (Controller/Service 会默认设置为 'active')

    private String prodCat; // 产品大类 (必填)

    private String prodPcat; // 产品小类 (必填)

    private String unitInfo; // 产品单位 (必填)

    private String specInfo; // 产品信息

    private String place; // 产地

}