package com.example.agricultural_product.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.agricultural_product.pojo.Product;
import com.example.agricultural_product.service.ProductService;
import com.example.agricultural_product.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    // ===== JWT 鉴权方法 =====
    private boolean checkToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return false;
        }
        String token = authHeader.substring(7);
        return JwtUtil.validateToken(token);
    }

    // 获取商品列表
    @GetMapping
    public ResponseEntity<Page<Product>> getProducts(HttpServletRequest request,
                                                     @RequestParam(defaultValue = "1") Integer pageNum,
                                                     @RequestParam(defaultValue = "10") Integer pageSize) {
        if (!checkToken(request)) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(productService.getProductList(pageNum, pageSize));
    }

    // 搜索商品
    @GetMapping("/search")
    public ResponseEntity<Page<Product>> searchProducts(HttpServletRequest request,
                                                        @RequestParam String keyword,
                                                        @RequestParam(defaultValue = "1") Integer pageNum,
                                                        @RequestParam(defaultValue = "10") Integer pageSize) {
        if (!checkToken(request)) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(productService.searchProducts(keyword, pageNum, pageSize));
    }

    // 获取某个农户的商品
    @GetMapping("/farmer/{farmerId}")
    public ResponseEntity<Page<Product>> getProductsByFarmer(HttpServletRequest request,
                                                             @PathVariable Long farmerId,
                                                             @RequestParam(defaultValue = "1") Integer pageNum,
                                                             @RequestParam(defaultValue = "10") Integer pageSize) {
        if (!checkToken(request)) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(productService.getProductsByFarmerId(farmerId, pageNum, pageSize));
    }

    // 获取单个商品详情
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(HttpServletRequest request,
                                              @PathVariable Integer id) {
        if (!checkToken(request)) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(productService.getById(id));
    }

    // 发布商品
    @PostMapping("/publish")
    public ResponseEntity<Boolean> publishProduct(HttpServletRequest request,
                                                  @RequestBody Product product) {
        if (!checkToken(request)) return ResponseEntity.status(401).build();
        boolean success = productService.publishProduct(product);
        return ResponseEntity.ok(success);
    }

    // 修改商品库存
    @PutMapping("/{id}/stock")
    public ResponseEntity<Boolean> updateStock(HttpServletRequest request,
                                               @PathVariable("id") Integer productId,
                                               @RequestParam("stock") Integer stock) {
        if (!checkToken(request)) return ResponseEntity.status(401).build();
        boolean success = productService.updateStock(productId, stock);
        return ResponseEntity.ok(success);
    }
}
