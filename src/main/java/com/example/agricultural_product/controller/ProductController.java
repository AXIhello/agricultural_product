package com.example.agricultural_product.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.agricultural_product.pojo.Product;
import com.example.agricultural_product.service.ProductService;
import com.example.agricultural_product.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class); 

    @Autowired
    private ProductService productService;

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    // ===== JWT 鉴权方法 =====
    private boolean checkToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return false;
        }
        String token = authHeader.substring(7);
        return JwtUtil.validateToken(token);
    }

    private Long getUserIdFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            // 假设JwtUtil.getUserIdFromToken会校验token并返回用户ID
            // 如果token无效，它应该返回null或抛出异常
            return JwtUtil.getUserId(token); // <-- 你需要确保JwtUtil中有这个方法
        }
        return null;
    }

    // 获取商品列表（不返回路径）
    @GetMapping
    public ResponseEntity<Page<Product>> getProducts(HttpServletRequest request,
                                                     @RequestParam(defaultValue = "1") Integer pageNum,
                                                     @RequestParam(defaultValue = "10") Integer pageSize) {
        if (!checkToken(request)) return ResponseEntity.status(401).build();
        Page<Product> page = productService.getProductList(pageNum, pageSize);
        maskImagePath(page.getRecords());
        return ResponseEntity.ok(page);
    }

    // 搜索商品（不返回路径）
    @GetMapping("/search")
    public ResponseEntity<Page<Product>> searchProducts(HttpServletRequest request,
                                                        @RequestParam String keyword,
                                                        @RequestParam(defaultValue = "1") Integer pageNum,
                                                        @RequestParam(defaultValue = "10") Integer pageSize) {
        if (!checkToken(request)) return ResponseEntity.status(401).build();
        Page<Product> page = productService.searchProducts(keyword, pageNum, pageSize);
        maskImagePath(page.getRecords());
        return ResponseEntity.ok(page);
    }

    // 获取某个农户的商品（不返回路径）
    @GetMapping("/farmer/{farmerId}")
    public ResponseEntity<Page<Product>> getProductsByFarmer(HttpServletRequest request,
                                                             @PathVariable Long farmerId,
                                                             @RequestParam(defaultValue = "1") Integer pageNum,
                                                             @RequestParam(defaultValue = "10") Integer pageSize) {
        if (!checkToken(request)) return ResponseEntity.status(401).build();
        Page<Product> page = productService.getProductsByFarmerId(farmerId, pageNum, pageSize);
        maskImagePath(page.getRecords());
        return ResponseEntity.ok(page);
    }

    // 获取单个商品详情（不返回路径）
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(HttpServletRequest request,
                                              @PathVariable Integer id) {
        if (!checkToken(request)) return ResponseEntity.status(401).build();
        Product p = productService.getById(id);
        if (p != null) {
            p.setImagePath(null);
        }
        return ResponseEntity.ok(p);
    }

    // 获取商品图片（二进制返回）
    @GetMapping("/{id}/image")
    public ResponseEntity<?> getProductImage(HttpServletRequest request,
                                             @PathVariable Integer id) {
        if (!checkToken(request)) return ResponseEntity.status(401).build();
        Product p = productService.getById(id);
        if (p == null || p.getImagePath() == null || p.getImagePath().trim().isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        try {
            // image_path 保存的是对外访问路径 /files/products/xxx.ext => 映射到本地 uploads/products/xxx.ext
            // 为避免硬编码前缀，做健壮切换：如果是以 "/files/" 开头，替换为本地目录；否则认为是本地相对路径已存储
            String imagePath = p.getImagePath();
            Path filePath;
            if (imagePath.startsWith("/files/")) {
                String relative = imagePath.substring("/files/".length());
                filePath = Paths.get(uploadDir).resolve(relative).normalize().toAbsolutePath();
            } else {
                filePath = Paths.get(uploadDir).resolve(imagePath).normalize().toAbsolutePath();
            }
            if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
                return ResponseEntity.notFound().build();
            }
            byte[] bytes = Files.readAllBytes(filePath);
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            return ResponseEntity.ok()
                    .header(HttpHeaders.CACHE_CONTROL, "max-age=86400, public")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(new ByteArrayResource(bytes));
        } catch (IOException io) {
            return ResponseEntity.internalServerError().body("Failed to read image");
        }
    }

    // =====【已修改】发布商品（multipart，图片可选）=====
    @PostMapping(value = "/publish", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Boolean> publishProductWithImage(HttpServletRequest request,
                                                           @RequestPart("product") Product product,
                                                           @RequestPart(value = "image", required = false) MultipartFile image) {

        // 1. 从Token中获取用户ID
        Long farmerId = getUserIdFromToken(request);
        if (farmerId == null) {
            // 如果token无效或无法解析出用户ID，则返回未授权
            return ResponseEntity.status(401).build();
        }

        // 2. 将获取到的ID设置到product对象中，这是解决问题的关键！
        product.setFarmerId(farmerId);

        log.info("【后端最终诊断】Controller 接收到的并设置了 farmerId 的 Product 对象: {}", product.toString());

        try {
            if (image != null && !image.isEmpty()) {
                Path dir = Paths.get(uploadDir, "products").toAbsolutePath().normalize();
                Files.createDirectories(dir);
                String original = image.getOriginalFilename();
                String ext = (original != null && original.lastIndexOf('.') >= 0) ? original.substring(original.lastIndexOf('.')) : "";
                String filename = UUID.randomUUID().toString().replace("-", "") + ext;
                Path target = dir.resolve(filename);
                Files.copy(image.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
                product.setImagePath("products/" + filename);
            }
        } catch (Exception e) {
            log.error("图片上传失败，但不影响商品发布流程", e);
        }

        // 3. 调用服务进行保存
        boolean success = productService.publishProduct(product);
        return ResponseEntity.ok(success);
    }

    // =====【已修改】发布商品（原JSON接口，保持兼容）=====
    @PostMapping(value = "/publish", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> publishProduct(HttpServletRequest request,
                                                  @RequestBody Product product) {
        
        // 1. 同样地，从Token中获取用户ID
        Long farmerId = getUserIdFromToken(request);
        if (farmerId == null) {
            return ResponseEntity.status(401).build();
        }

        // 2. 设置farmerId
        product.setFarmerId(farmerId);
        log.info("【后端JSON接口】设置了 farmerId 的 Product 对象: {}", product.toString());
        
        // 3. 调用服务
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

    private void maskImagePath(List<Product> records) {
        if (records == null) return;
        records.forEach(p -> { if (p != null) p.setImagePath(null); });
    }
}
