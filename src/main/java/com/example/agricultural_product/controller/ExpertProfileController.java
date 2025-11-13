package com.example.agricultural_product.controller;

import com.example.agricultural_product.dto.ExpertInfoDTO;
import com.example.agricultural_product.pojo.ExpertProfile;
import com.example.agricultural_product.pojo.Result;
import com.example.agricultural_product.service.ExpertProfileService;
import com.example.agricultural_product.service.FileStorageService;
import com.example.agricultural_product.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;

// import org.junit.runner.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal; 
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/expert/profile")
public class ExpertProfileController {

    @Autowired
    private ExpertProfileService expertProfileService;

    @Autowired
    private FileStorageService fileStorageService;


    // JWT 鉴权方法
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
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7);
        return JwtUtil.getUserId(token);
    }

    /**
     * 创建或更新专家档案
     * POST /api/expert/profile
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createOrUpdateProfile(
            HttpServletRequest request,
            @RequestParam("specialization") String specialization,
            @RequestParam("bio") String bio,
            @RequestParam("consultationFee") BigDecimal consultationFee,
            // 文件是可选的
            @RequestParam(value = "photo", required = false) MultipartFile photo) {
        
        if (!checkToken(request)) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "未授权"));
        }

        Long expertId = getUserIdFromToken(request);
        if (expertId == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "无法获取用户ID"));
        }

        // 创建一个 ExpertProfile 对象并填充数据
        ExpertProfile profile = new ExpertProfile();
        profile.setSpecialization(specialization);
        profile.setBio(bio);
        profile.setConsultationFee(consultationFee);

        // 处理文件上传
        if (photo != null && !photo.isEmpty()) {
            try {
                String photoUrl = fileStorageService.storeFile(photo); // 调用文件存储服务
                profile.setPhotoUrl(photoUrl);
            } catch (Exception e) {
                 return ResponseEntity.status(500).body(Map.of("success", false, "message", "文件上传失败: " + e.getMessage()));
            }
        }

        try {
            ExpertProfile saved = expertProfileService.saveOrUpdateProfile(expertId, profile);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "档案保存成功");
            result.put("data", saved);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false, 
                "message", "保存失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取当前专家的档案
     * GET /api/expert/profile
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getMyProfile(HttpServletRequest request) {
        if (!checkToken(request)) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "未授权"));
        }

        Long expertId = getUserIdFromToken(request);
        ExpertProfile profile = expertProfileService.getProfile(expertId);
        
        if (profile == null) {
            return ResponseEntity.status(404).body(Map.of(
                "success", false, 
                "message", "档案不存在，请先创建"
            ));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", profile);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取指定专家的档案（公开访问）
     * GET /api/expert/profile/{expertId}
     */
    @GetMapping("/{expertId}")
    public ResponseEntity<Map<String, Object>> getProfileById(@PathVariable Long expertId) {
        ExpertProfile profile = expertProfileService.getProfile(expertId);
        
        if (profile == null) {
            return ResponseEntity.status(404).body(Map.of(
                "success", false, 
                "message", "专家档案不存在"
            ));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", profile);
        return ResponseEntity.ok(result);
    }

    /**
     * 删除专家档案
     * DELETE /api/expert/profile
     */
    @DeleteMapping
    public ResponseEntity<Map<String, Object>> deleteProfile(HttpServletRequest request) {
        if (!checkToken(request)) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "未授权"));
        }

        Long expertId = getUserIdFromToken(request);
        boolean deleted = expertProfileService.deleteProfile(expertId);
        
        return ResponseEntity.ok(Map.of(
            "success", deleted,
            "message", deleted ? "删除成功" : "删除失败"
        ));
    }

    /**
     * 列出所有专家的基本信息（公开访问）
     * GET /api/expert/profile/list
     */
    @GetMapping("/list") 
    public Result<List<ExpertInfoDTO>> listExperts() {
        List<ExpertInfoDTO> experts = expertProfileService.getAllExperts();
        return Result.success(experts); // 假设你有一个统一的 Result 封装类
    }
}