package com.example.agricultural_product.dto;

import lombok.Data;

@Data
public class UpdateBankProductStatusRequest {
    // 取值：active / inactive
    private String status;
}