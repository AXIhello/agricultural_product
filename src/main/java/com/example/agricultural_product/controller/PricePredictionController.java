package com.example.agricultural_product.controller;

import com.example.agricultural_product.dto.PredictionRequest;
import com.example.agricultural_product.service.PricePredictionService;
import com.example.agricultural_product.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/predictions")
public class PricePredictionController {

    @Autowired
    private PricePredictionService predictionService;

    // JWT鉴权方法（复用现有逻辑）
    private boolean checkToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return false;
        }
        String token = authHeader.substring(7);
        return JwtUtil.validateToken(token);
    }

    /**
     * 核心接口：启动预测任务或执行 CRUD，并立即返回任务ID或成功标志。
     * HTTP 状态码返回 202 Accepted 表示请求已接受。
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> startPredictionOrCrud(
            HttpServletRequest request,
            @RequestBody PredictionRequest predictionRequest) {

        if (!checkToken(request)) {
            return ResponseEntity.status(401).build();
        }

        try {
            // Service 层将同步处理 CRUD 或异步启动预测任务
            String taskIdOrStatus = predictionService.startPredictionTask(predictionRequest);

            if ("CRUD_SUCCESS".equals(taskIdOrStatus)) {
                // 如果是数据库操作，立即返回成功
                return ResponseEntity.ok(Map.of("status", "SUCCESS", "message", "商品信息更新成功"));
            } else {
                // 如果是预测任务，返回任务ID和 202 状态码 (Accepted)
                return ResponseEntity.status(HttpStatus.ACCEPTED)
                        .body(Map.of("status", "RUNNING", "taskId", taskIdOrStatus, "message", "预测任务已启动，请稍后查询结果"));
            }
        } catch (IllegalArgumentException e) {
            // 捕获 Service 层抛出的业务校验错误
            return ResponseEntity.badRequest().body(Map.of("status", "FAILED", "message", e.getMessage()));
        } catch (Exception e) {
            // 捕获启动任务时的其他异常
            return ResponseEntity.internalServerError().body(Map.of("status", "ERROR", "message", "启动预测任务失败"));
        }
    }

    /**
     * 【新增】查询预测任务结果或状态的接口。
     */
    @GetMapping("/status/{taskId}")
    public ResponseEntity<Map<String, Object>> getPredictionStatus(
            HttpServletRequest request,
            @PathVariable String taskId) {

        if (!checkToken(request)) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> result = predictionService.getPredictionResult(taskId);
        String status = (String) result.getOrDefault("status", "NOT_FOUND");

        switch (status) {
            case "COMPLETED":
                // 成功完成，返回 200 OK，包含数据
                return ResponseEntity.ok(result);
            case "RUNNING":
                // 任务仍在进行，返回 202 Accepted，客户端应继续轮询
                return ResponseEntity.status(HttpStatus.ACCEPTED).body(result);
            case "FAILED":
                // 任务失败，返回 500 Internal Server Error (或 400 视具体错误)
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
            case "NOT_FOUND":
            default:
                // 任务ID不存在
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("status", "NOT_FOUND", "message", "任务ID不存在或已过期"));
        }
    }

    // 获取可预测的产品列表 (保持不变)
    @GetMapping("/products")
    public ResponseEntity<List<String>> getPredictableProducts(HttpServletRequest request) {
        if (!checkToken(request)) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(predictionService.getPredictableProducts());
    }

    // 获取元数据 (保持不变)
    @GetMapping("/metadata/search")
    public ResponseEntity<Map<String, List<String>>> searchProductMetadata(
            HttpServletRequest request,
            @RequestParam("productName") String productName) {

        if (!checkToken(request)) {
            return ResponseEntity.status(401).build();
        }

        if (productName == null || productName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", List.of("产品名称不能为空")));
        }

        Map<String, List<String>> metadata = predictionService.getProductCategoryAndSpec(productName);

        if (metadata.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(metadata);
    }
}