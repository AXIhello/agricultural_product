package com.example.agricultural_product.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.agricultural_product.dto.CreateBankProductRequest;
import com.example.agricultural_product.dto.UpdateBankProductStatusRequest;
import com.example.agricultural_product.pojo.BankProduct;
import com.example.agricultural_product.service.BankProductService;
import com.example.agricultural_product.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

    /**
     * 删除银行产品
     * URL: /api/bankProducts/{productId}
     * 仅允许发布者本人删除
     */
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(HttpServletRequest request,
                                              @PathVariable Integer productId) {
        // 1. 从 token 中获取当前银行用户 ID
        Long bankUserId = getUserIdFromToken(request);

        // 2. 调用 Service 删除产品
        boolean success = bankProductService.deleteProduct(bankUserId, productId);

        // 3. 返回结果
        if (success) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(403).build(); // 无权限删除或产品不存在
        }
    }

}