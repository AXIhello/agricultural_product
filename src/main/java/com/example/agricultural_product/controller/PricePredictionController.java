package com.example.agricultural_product.controller;

import com.example.agricultural_product.dto.PredictionRequest;
import com.example.agricultural_product.service.PricePredictionService;
import com.example.agricultural_product.utils.JwtUtil;
import org.slf4j.Logger; 
import org.slf4j.LoggerFactory; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/predictions")
public class PricePredictionController {

    private static final Logger logger = LoggerFactory.getLogger(PricePredictionController.class);

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
            logger.warn("请求参数校验失败: {}", e.getMessage());
            // 捕获 Service 层抛出的业务校验错误
            return ResponseEntity.badRequest().body(Map.of("status", "FAILED", "message", e.getMessage()));
        } catch (Exception e) {
            // 捕获启动任务时的其他异常
            logger.error("启动预测任务时发生系统异常", e);
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
                // 成功完成，返回 200 OK
                return ResponseEntity.ok(result);

            case "RUNNING":
                // 任务仍在进行，返回 202 Accepted
                return ResponseEntity.status(HttpStatus.ACCEPTED).body(result);

            case "FAILED":
                // 【修改点】任务失败
                // 这是一个正常的业务反馈（即“告诉前端任务失败了”这个动作本身是成功的）
                // 所以应该返回 200 OK，让前端通过 body 中的 status: FAILED 来判断业务逻辑
                String errorMessage = (String) result.getOrDefault("message", "未知错误");
                // 依然可以打印 error 日志方便排查，或者降级为 warn
                logger.warn("预测任务业务失败 [TaskId: {}]: {}", taskId, errorMessage);

                // 改为返回 200 OK
                return ResponseEntity.ok(result);

            case "NOT_FOUND":
            default:
                // 任务ID不存在，返回 404
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

    // 获取元数据
    @GetMapping("/metadata/search")
    public ResponseEntity<Map<String, List<String>>> searchProductMetadata(
            HttpServletRequest request,
            @RequestParam("productName") String productName) {

        if (!checkToken(request)) {
            return ResponseEntity.status(401).build();
        }

        if (productName == null || productName.trim().isEmpty()) {
            // 参数为空还是算作请求错误，可以保留 400，或者也改成返回空
            return ResponseEntity.badRequest().body(null);
        }

        // 调用 Service 查询
        Map<String, List<String>> metadata = predictionService.getProductCategoryAndSpec(productName);


        // 即使没找到，返回 200 OK，但是内容是空的列表
        if (metadata.isEmpty()) {
            // JSON 结果: { "specs": [], "pcats": [] }
            return ResponseEntity.ok(Map.of(
                    "specs", Collections.emptyList(),
                    "pcats", Collections.emptyList()
            ));
        }

        return ResponseEntity.ok(metadata);
    }
}