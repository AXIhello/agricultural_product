package com.example.agricultural_product.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.agricultural_product.dto.ProductUpdateDTO;
import com.example.agricultural_product.dto.RecommendedProductDTO;
import com.example.agricultural_product.pojo.Product;
import com.example.agricultural_product.service.ProductService;
import com.example.agricultural_product.utils.JwtUtil;
import com.example.agricultural_product.vo.ProductTreeVO;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    // 从 Token 中获取角色
    private String getUserRoleFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7);
        return JwtUtil.getRole(token);
    }

    private boolean isAdmin(HttpServletRequest request) {
        if (!checkToken(request)) {
            return false;
        }
        String role = getUserRoleFromToken(request);
        return "admin".equalsIgnoreCase(role);
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

            String originalPath = p.getImagePath();

            boolean hasImg = originalPath != null && !originalPath.trim().isEmpty();

            p.setHasImage(hasImg);

            p.setImagePath(null); // 屏蔽图片路径

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

    /**
     * 局部更新商品图片
     * URL: /api/products/{id}/image
     * Content-Type: multipart/form-data
     */
    @PatchMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Boolean> updateProductImage(HttpServletRequest request,
                                                      @PathVariable("id") Integer productId,
                                                      @RequestPart("image") MultipartFile image) {

        // 1. 权限校验
        // 检查 Token 是否有效
        if (!checkToken(request)) {
            return ResponseEntity.status(401).body(false); // 401 Unauthorized
        }

        // 获取用户ID，用于权限判断：确保农户只能修改自己的商品
        Long farmerId = getUserIdFromToken(request);
        if (farmerId == null) {
            return ResponseEntity.status(401).body(false);
        }

        // 2. 文件上传与路径生成
        String newImagePath = null;
        try {
            if (image == null || image.isEmpty()) {
                // 允许传入空文件表示删除图片？或者直接返回错误？这里选择返回错误
                return ResponseEntity.badRequest().body(false);
            }

            // 文件存储逻辑：路径与您现有的 /publish 接口保持一致
            Path dir = Paths.get(uploadDir, "products").toAbsolutePath().normalize();
            Files.createDirectories(dir);

            String original = image.getOriginalFilename();
            String ext = (original != null && original.lastIndexOf('.') >= 0) ? original.substring(original.lastIndexOf('.')) : "";
            String filename = UUID.randomUUID().toString().replace("-", "") + ext;
            Path target = dir.resolve(filename);

            // 执行文件拷贝/上传
            Files.copy(image.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            // 存储到数据库的相对路径（例如: products/xxxx.jpg）
            newImagePath = "products/" + filename;

        } catch (Exception e) {
            log.error("更新商品ID: {} 的图片上传失败", productId, e);
            return ResponseEntity.internalServerError().body(false);
        }

        // 3. 调用 Service 方法更新数据库记录
        // Service 方法需要同时接收 farmerId 来进行权限检查
        boolean success = productService.updateImagePath(productId, farmerId, newImagePath);

        if (!success) {
            // 如果失败，可能是 productId 不存在，或者 farmerId 无权操作
            return ResponseEntity.status(403).body(false); // 403 Forbidden 或其他更具体的错误
        }

        return ResponseEntity.ok(true);
    }

    /**
     * 更新商品基础信息
     * URL: /api/products/{id}
     * Content-Type: application/json
     */
    @PutMapping("/{id}")
    public ResponseEntity<Boolean> updateProduct(HttpServletRequest request,
                                                 @PathVariable("id") Integer productId,
                                                 @RequestBody ProductUpdateDTO updateDTO) {
        // 1. 权限校验
        if (!checkToken(request)) {
            return ResponseEntity.status(401).body(false);
        }

        Long farmerId = getUserIdFromToken(request);
        if (farmerId == null) {
            return ResponseEntity.status(401).body(false);
        }

        // 确保路径中的 ID 与请求体中的 ID 匹配
        if (!productId.equals(updateDTO.getProductId())) {
            return ResponseEntity.badRequest().body(false);
        }

        // 2. 调用 Service 执行更新
        boolean success = productService.updateProductInfo(updateDTO, farmerId);

        if (!success) {
            // 如果更新失败，可能是权限问题或商品不存在
            return ResponseEntity.status(403).body(false); // 403 Forbidden
        }

        return ResponseEntity.ok(true);
    }

    /**
     * 删除商品
     * URL: DELETE /api/products/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteProduct(HttpServletRequest request,
                                                 @PathVariable("id") Integer productId) {
        // 1. 权限校验
        if (!checkToken(request)) {
            return ResponseEntity.status(401).body(false);
        }

        Long farmerId = getUserIdFromToken(request);
        if (farmerId == null) {
            return ResponseEntity.status(401).body(false);
        }

        // 2. 调用 Service 执行删除
        boolean success = productService.deleteProduct(productId, farmerId);

        if (!success) {
            // 如果删除失败，可能是权限问题 (403) 或商品不存在 (404)
            // 简单处理：返回 403 Forbidden
            return ResponseEntity.status(403).body(false);
        }

        return ResponseEntity.ok(true);
    }

    /**
     * 快捷更新商品状态（上架/下架）
     * URL: PUT /api/products/{id}/status
     * Body: {"status": "active" or "inactive"}
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<Boolean> updateProductStatus(HttpServletRequest request,
                                                       @PathVariable("id") Integer productId,
                                                       @RequestParam("status") String status) {
        // 1. 权限校验
        if (!checkToken(request)) {
            return ResponseEntity.status(401).body(false);
        }

        Long farmerId = getUserIdFromToken(request);
        if (farmerId == null) {
            return ResponseEntity.status(401).body(false);
        }


        // 2. 调用 Service 执行状态更新
        boolean success = productService.updateProductStatus(productId, farmerId, status);

        if (!success) {
            // 如果更新失败，可能是权限问题 (403) 或商品不存在 (404)
            return ResponseEntity.status(403).body(false);
        }

        return ResponseEntity.ok(true);
    }

    /**
     * URL: /api/products/all
     * *当前登录用户查询商品。不进行名字筛选
     * 允许参数 status="active", "inactive", 或不传（查询全部）。不进行名字筛选
     */
    @GetMapping("/all")
    public ResponseEntity<Page<Product>> getAllProducts(HttpServletRequest request,
                                                        @RequestParam(value = "status", required = false) String status,
                                                        @RequestParam(defaultValue = "1") Integer pageNum,
                                                        @RequestParam(defaultValue = "10") Integer pageSize) {
        // 通常此接口用于管理员后台或特定场景，需要鉴权
        if (!checkToken(request)) return ResponseEntity.status(401).build();

        Page<Product> page = productService.getAllProductsListByStatus(status, pageNum, pageSize);
        maskImagePath(page.getRecords());
        return ResponseEntity.ok(page);
    }


    /**
     * URL: /api/products/farmer
     * 仅供当前登录农户查询自己的商品。不进行名字筛选
     * 允许参数 status="active", "inactive", 或不传（查询全部）
     */
    @GetMapping("/farmer")
    public ResponseEntity<Page<Product>> getFarmerProductsByStatus(HttpServletRequest request,
                                                               @RequestParam(value = "status", required = false) String status,
                                                               @RequestParam(defaultValue = "1") Integer pageNum,
                                                               @RequestParam(defaultValue = "10") Integer pageSize) {
        // 1. 权限校验并获取农户ID
        Long farmerId = getUserIdFromToken(request);
        if (farmerId == null) {
            return ResponseEntity.status(401).build();
        }

        // 2. 调用服务
        Page<Product> page = productService.getProductsByFarmerIdAndStatusPage(farmerId, status, pageNum, pageSize);
        maskImagePath(page.getRecords());
        return ResponseEntity.ok(page);
    }

    /**
     * URL: /api/products/farmer
     * 仅供当前登录用户查询商品。进行名字筛选
     * 允许参数 status="active", "inactive", 或不传（查询全部）
     */
    @GetMapping("/productName")
    public ResponseEntity<Page<Product>> getProductsByStatus(
            HttpServletRequest request,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "name", required = false) String productName,
            @RequestParam(value = "createTime", required = false) String createTime,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        if (status == null) status = "active";

        Page<Product> page;

        if ((productName == null || productName.trim().isEmpty()) &&
                (createTime == null || createTime.trim().isEmpty())) {
            // 阶段一：无名称与时间 → 返回名称去重列表
            page = productService.selectDistinctProductsByStatusPage(status, pageNum, pageSize);
        } else {
            // 阶段二：传入名称或时间 → 返回筛选结果
            log.info("【查询】状态:{}，商品名:{}，时间:{}，分页商品实体列表。", status, productName, createTime);

            page = productService.getProductsByStatusPage(status, productName, createTime, pageNum, pageSize);
        }

        maskImagePath(page.getRecords());
        return ResponseEntity.ok(page);
    }


    /**
     * URL: /api/products/farmer
     * 仅供当前登录农户查询自己的商品。进行名字筛选
     * 允许参数 status="active", "inactive", 或不传（查询全部）
     */
    @GetMapping("/farmerProductName")
    public ResponseEntity<Page<Product>> getFarmerProductsByStatus(
            HttpServletRequest request,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "name", required = false) String productName,
            @RequestParam(value = "createTime", required = false) String createTime,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        Long farmerId = getUserIdFromToken(request);
        if (farmerId == null) return ResponseEntity.status(401).build();

        if (status == null) status = "active";

        Page<Product> page;

        if ((productName == null || productName.trim().isEmpty()) &&
                (createTime == null || createTime.trim().isEmpty())) {

            log.info("【查询】农户:{} 状态:{} 请求去重商品名称列表", farmerId, status);
            page = productService.selectDistinctProductsByFarmerIdAndStatusPage(farmerId, status, pageNum, pageSize);

        } else {

            log.info("【查询】农户:{} 状态:{} 名称:{} 时间:{} 请求实体列表",
                    farmerId, status, productName, createTime);

            page = productService.getProductsByFarmerIdAndStatusPage(farmerId, status, productName, createTime, pageNum, pageSize);
        }

        maskImagePath(page.getRecords());
        return ResponseEntity.ok(page);
    }


    /*
    后端：执行 SQL 查出数据库里 1000 条商品，组装成一个巨大的 JSON，一次性发给前端。
    前端获取到了什么？
    前端拿到了完整的数据包：包含所有的分类、所有的子分类、所有的商品名、以及最底层具体的商品详情（ID、价格、库存等）。
    界面显示：
    虽然手里拿着所有数据，但在 UI 上，通常只渲染第一层（水果、水产）。其他的存在内存里，暂时隐藏（display: none）。
     */
    /**
     * URL: /api/products/tree
     * 获取商品分类树形结构 (一级 -> 二级 -> 名称 -> 商品列表)，所有人都可以看
     */
    @GetMapping("/tree")
    public ResponseEntity<List<ProductTreeVO>> getProductTree(
            HttpServletRequest request,
            @RequestParam(value = "status", required = false, defaultValue = "active") String status) {

        // 1. 鉴权 (如果需要登录才能看)
         if (!checkToken(request)) return ResponseEntity.status(401).build();

        // 2. 调用 Service 获取树形结构
        List<ProductTreeVO> tree = productService.getProductTreeByStatus(status);

        return ResponseEntity.ok(tree);
    }

    /**
     * URL: /api/products/farmer/tree
     * 作用: 获取商品分类树形结构 (一级 -> 二级 -> 名称 -> 商品列表)，仅获取【当前登录农户】发布的商品树形结构
     */
    @GetMapping("/farmer/tree")
    public ResponseEntity<List<ProductTreeVO>> getFarmerProductTree(
            HttpServletRequest request,
            @RequestParam(value = "status", required = false, defaultValue = "active") String status) {

        // 1. 必须鉴权：检查 Token 是否合法
        if (!checkToken(request)) {
            return ResponseEntity.status(401).build();
        }

        // 2. 获取 Token 中的 FarmerID
        Long farmerId = getUserIdFromToken(request);
        if (farmerId == null) {
            return ResponseEntity.status(401).build();
        }

        log.info("【农户树形查询】用户ID: {}, 状态: {}", farmerId, status);

        // 3. 调用 Service 新增的方法
        List<ProductTreeVO> tree = productService.getProductTreeByFarmerIdAndStatus(farmerId, status);

        return ResponseEntity.ok(tree);
    }

    // ===== 商品推荐相关接口 =====

    /**
     * 智能推荐商品
     * 基于评价统计字段的推荐算法
     */
    @GetMapping("/recommend")
    public ResponseEntity<Page<RecommendedProductDTO>> recommendProducts(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String category) {
        
        // 获取用户ID（可选，用于个性化推荐）
        Long userId = getUserIdFromToken(request);
        
        Page<RecommendedProductDTO> recommendedProducts = productService.recommendProducts(
                userId, pageNum, pageSize, category);
        
        return ResponseEntity.ok(recommendedProducts);
    }

    /**
     * 获取热销商品推荐
     * 基于评价数量排序
     */
    @GetMapping("/hot")
    public ResponseEntity<Page<RecommendedProductDTO>> getHotProducts(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        Page<RecommendedProductDTO> hotProducts = productService.getHotProducts(pageNum, pageSize);
        
        return ResponseEntity.ok(hotProducts);
    }

    /**
     * 获取高评分商品推荐
     * 基于平均评分排序
     */
    @GetMapping("/high-rated")
    public ResponseEntity<Page<RecommendedProductDTO>> getHighRatedProducts(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(defaultValue = "5") Integer minRatingCount) {
        
        Page<RecommendedProductDTO> highRatedProducts = productService.getHighRatedProducts(
                pageNum, pageSize, minRatingCount);
        
        return ResponseEntity.ok(highRatedProducts);
    }

    // ========== 管理员商品管理相关接口 ==========

    /**
     * 管理员分页查看所有商品（可带状态过滤）
     * status 可选：active / inactive / null(全部)
     */
    @GetMapping("/admin/list")
    public ResponseEntity<?> adminListProducts(HttpServletRequest request,
                                               @RequestParam(defaultValue = "1") Integer pageNum,
                                               @RequestParam(defaultValue = "10") Integer pageSize,
                                               @RequestParam(required = false) String status) {
        if (!isAdmin(request)) {
            return ResponseEntity.status(403)
                    .body(java.util.Map.of("success", false, "message", "无权限，只有管理员可以访问该接口"));
        }
        Page<Product> page = productService.getAllProductsListByStatus(status, pageNum, pageSize);
        // 管理端可以看到图片路径，这里不做 mask
        return ResponseEntity.ok(page);
    }

    /**
     * 管理员查看单个商品详情
     */
    @GetMapping("/admin/{productId}")
    public ResponseEntity<?> adminGetProduct(HttpServletRequest request,
                                             @PathVariable Integer productId) {
        if (!isAdmin(request)) {
            return ResponseEntity.status(403)
                    .body(java.util.Map.of("success", false, "message", "无权限，只有管理员可以访问该接口"));
        }
        Product product = productService.getById(productId);
        if (product == null) {
            return ResponseEntity.status(404)
                    .body(java.util.Map.of("success", false, "message", "商品不存在"));
        }
        return ResponseEntity.ok(java.util.Map.of("success", true, "product", product));
    }

    /**
     * 管理员更新商品信息（不受 farmerId 限制）
     * 只更新 DTO 中非空字段
     */
    @PutMapping("/admin/{productId}")
    public ResponseEntity<?> adminUpdateProduct(HttpServletRequest request,
                                                @PathVariable Integer productId,
                                                @RequestBody ProductUpdateDTO updateDTO) {
        if (!isAdmin(request)) {
            return ResponseEntity.status(403)
                    .body(java.util.Map.of("success", false, "message", "无权限，只有管理员可以访问该接口"));
        }
        Product existing = productService.getById(productId);
        if (existing == null) {
            return ResponseEntity.status(404)
                    .body(java.util.Map.of("success", false, "message", "商品不存在"));
        }
        // 手动根据 DTO 非空值更新字段
        if (updateDTO.getProductName() != null) {
            existing.setProductName(updateDTO.getProductName());
        }
        if (updateDTO.getDescription() != null) {
            existing.setDescription(updateDTO.getDescription());
        }
        if (updateDTO.getPrice() != null) {
            existing.setPrice(updateDTO.getPrice());
        }
        if (updateDTO.getStock() != null) {
            existing.setStock(updateDTO.getStock());
        }
        if (updateDTO.getStatus() != null) {
            existing.setStatus(updateDTO.getStatus());
        }
        if (updateDTO.getProdCat() != null) {
            existing.setProdCat(updateDTO.getProdCat());
        }
        if (updateDTO.getProdPcat() != null) {
            existing.setProdPcat(updateDTO.getProdPcat());
        }
        if (updateDTO.getUnitInfo() != null) {
            existing.setUnitInfo(updateDTO.getUnitInfo());
        }
        if (updateDTO.getSpecInfo() != null) {
            existing.setSpecInfo(updateDTO.getSpecInfo());
        }
        if (updateDTO.getPlace() != null) {
            existing.setPlace(updateDTO.getPlace());
        }
        existing.setUpdateTime(java.time.LocalDateTime.now());
        boolean ok = productService.updateById(existing);
        if (!ok) {
            return ResponseEntity.internalServerError()
                    .body(java.util.Map.of("success", false, "message", "更新失败"));
        }
        return ResponseEntity.ok(java.util.Map.of("success", true, "product", existing));
    }

    /**
     * 管理员删除商品（物理删除）
     */
    @DeleteMapping("/admin/{productId}")
    public ResponseEntity<?> adminDeleteProduct(HttpServletRequest request,
                                                @PathVariable Integer productId) {
        if (!isAdmin(request)) {
            return ResponseEntity.status(403)
                    .body(java.util.Map.of("success", false, "message", "无权限，只有管理员可以访问该接口"));
        }
        boolean ok = productService.removeById(productId);
        if (!ok) {
            return ResponseEntity.status(404)
                    .body(java.util.Map.of("success", false, "message", "商品不存在或删除失败"));
        }
        return ResponseEntity.ok(java.util.Map.of("success", true, "message", "删除成功"));
    }

}
