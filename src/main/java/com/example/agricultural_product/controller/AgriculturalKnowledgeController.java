package com.example.agricultural_product.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.agricultural_product.pojo.AgriculturalKnowledge;
import com.example.agricultural_product.service.AgriculturalKnowledgeService;
import com.example.agricultural_product.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/knowledge")
public class AgriculturalKnowledgeController {

    @Autowired
    private AgriculturalKnowledgeService knowledgeService;

    private Long getUserIdFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7);
        return JwtUtil.getUserId(token);
    }

    /**
     * 发布农业知识
     */
    @PostMapping("/publish")
    public ResponseEntity<Map<String, Object>> publishKnowledge(
            HttpServletRequest request,
            @RequestBody Map<String, String> params) {
        
        Long expertId = getUserIdFromToken(request);
        if (expertId == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "未授权"));
        }

        String title = params.get("title");
        String content = params.get("content");
        
        if (title == null || content == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "标题和内容不能为空"));
        }

        try {
            AgriculturalKnowledge knowledge = knowledgeService.publishKnowledge(expertId, title, content);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "发布成功");
            result.put("data", knowledge);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * 更新农业知识
     */
    @PutMapping("/{knowledgeId}")
    public ResponseEntity<Map<String, Object>> updateKnowledge(
            HttpServletRequest request,
            @PathVariable Integer knowledgeId,
            @RequestBody Map<String, String> params) {
        
        Long expertId = getUserIdFromToken(request);
        if (expertId == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "未授权"));
        }

        String title = params.get("title");
        String content = params.get("content");
        
        boolean updated = knowledgeService.updateKnowledge(expertId, knowledgeId, title, content);
        return ResponseEntity.ok(Map.of("success", updated));
    }

    /**
     * 删除农业知识
     */
    @DeleteMapping("/{knowledgeId}")
    public ResponseEntity<Map<String, Object>> deleteKnowledge(
            HttpServletRequest request,
            @PathVariable Integer knowledgeId) {
        
        Long expertId = getUserIdFromToken(request);
        if (expertId == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "未授权"));
        }

        boolean deleted = knowledgeService.deleteKnowledge(expertId, knowledgeId);
        return ResponseEntity.ok(Map.of("success", deleted));
    }

    /**
     * 获取专家发布的知识列表
     */
    @GetMapping("/expert/{expertId}")
    public ResponseEntity<Page<AgriculturalKnowledge>> getExpertKnowledgeList(
            @PathVariable Long expertId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        return ResponseEntity.ok(knowledgeService.getExpertKnowledgeList(expertId, pageNum, pageSize));
    }

    /**
     * 获取知识详情
     */
    @GetMapping("/{knowledgeId}")
    public ResponseEntity<AgriculturalKnowledge> getKnowledgeDetail(@PathVariable Integer knowledgeId) {
        return ResponseEntity.ok(knowledgeService.getKnowledgeDetail(knowledgeId));
    }
}