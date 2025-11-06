package com.example.agricultural_product.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.agricultural_product.dto.CreateBankProductRequest;
import com.example.agricultural_product.dto.UpdateBankProductStatusRequest;
import com.example.agricultural_product.pojo.BankProduct;
import com.example.agricultural_product.service.BankProductService;
import com.example.agricultural_product.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bank/products")
public class BankProductController {

    @Autowired
    private BankProductService bankProductService;

    // ===== JWT 鉴权工具方法 =====
    private Long getUserIdFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("未提供有效的 JWT token");
        }
        String token = authHeader.substring(7);
        if (!JwtUtil.validateToken(token)) {
            throw new RuntimeException("无效的 JWT token");
        }
        return JwtUtil.getUserId(token);
    }

    // 银行发布固定产品（bankUserId 从 token）
    @PostMapping
    public Integer createProduct(HttpServletRequest request,
                                 @RequestBody CreateBankProductRequest req) {
        Long bankUserId = getUserIdFromToken(request);
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
    public boolean updateStatus(HttpServletRequest request,
                                @PathVariable Integer productId,
                                @RequestBody UpdateBankProductStatusRequest req) {
        Long bankUserId = getUserIdFromToken(request);
        return bankProductService.updateStatus(bankUserId, productId, req.getStatus());
    }

    // 银行查看自己发布的产品列表（bankUserId 从 token）
    @GetMapping("/my")
    public Page<BankProduct> listMyProducts(HttpServletRequest request,
                                            @RequestParam(defaultValue = "1") Integer pageNum,
                                            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long bankUserId = getUserIdFromToken(request);
        return bankProductService.listMyProducts(bankUserId, pageNum, pageSize);
    }

    // 所有发布的产品列表（bankUserId 从 token）
    @GetMapping("/all")
    public Page<BankProduct> listAllProducts(@RequestParam(defaultValue = "1") Integer pageNum,
                                            @RequestParam(defaultValue = "10") Integer pageSize) {
        return bankProductService.listAllProducts(pageNum, pageSize);
    }
}