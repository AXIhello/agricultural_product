package com.example.agricultural_product.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ProductStatusDTO {

    // 仅用于接收前端传递的 status 字段
    private String status;
}