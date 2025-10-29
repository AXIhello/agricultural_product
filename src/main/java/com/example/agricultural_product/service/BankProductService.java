package com.example.agricultural_product.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.agricultural_product.pojo.BankProduct;

import java.math.BigDecimal;

public interface BankProductService {
    Integer createProduct(Long bankUserId, String name, String description,
                          Integer termMonths, BigDecimal interestRate,
                          BigDecimal minAmount, BigDecimal maxAmount);

    boolean updateStatus(Long bankUserId, Integer productId, String status); // active/inactive

    Page<BankProduct> listMyProducts(Long bankUserId, Integer pageNum, Integer pageSize);
}