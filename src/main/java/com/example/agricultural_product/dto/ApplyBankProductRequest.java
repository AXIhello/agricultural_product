package com.example.agricultural_product.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ApplyBankProductRequest {
    private Integer productId;
    private BigDecimal amount;
    private String purpose;
    private List<Long> coApplicantIds;
}