package com.example.agricultural_product.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateBankProductRequest {
    private String productName;
    private String description;
    private Integer termMonths;
    private BigDecimal interestRate;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
}