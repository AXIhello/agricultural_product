package com.example.agricultural_product.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.agricultural_product.dto.CreateBankProductRequest;
import com.example.agricultural_product.dto.UpdateBankProductStatusRequest;
import com.example.agricultural_product.pojo.BankProduct;
import com.example.agricultural_product.service.BankProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bank/products")
public class BankProductController {

    @Autowired
    private BankProductService bankProductService;

    // 银行发布固定产品（bankUserId 从 token）
    @PostMapping
    public Integer createProduct(@RequestAttribute("userId") Long bankUserId,
                                 @RequestBody CreateBankProductRequest req) {
        return bankProductService.createProduct(
                bankUserId,
                req.getProductName(),
                req.getDescription(),
                req.getTermMonths(),
                req.getInterestRate(),
                req.getMinAmount(),
                req.getMaxAmount()
        );
    }

    // 银行上下架产品（bankUserId 从 token）
    @PatchMapping("/{productId}/status")
    public boolean updateStatus(@RequestAttribute("userId") Long bankUserId,
                                @PathVariable Integer productId,
                                @RequestBody UpdateBankProductStatusRequest req) {
        return bankProductService.updateStatus(bankUserId, productId, req.getStatus());
    }

    // 银行查看自己发布的产品列表（bankUserId 从 token）
    @GetMapping("/my")
    public Page<BankProduct> listMyProducts(@RequestAttribute("userId") Long bankUserId,
                                            @RequestParam(defaultValue = "1") Integer pageNum,
                                            @RequestParam(defaultValue = "10") Integer pageSize) {
        return bankProductService.listMyProducts(bankUserId, pageNum, pageSize);
    }
}