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
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/expert/profile")
public class ExpertProfileController {

    private static final Logger log = LoggerFactory.getLogger(ExpertProfileController.class);

    @Autowired
    private ExpertProfileService expertProfileService;

    @Autowired
    private FileStorageService fileStorageService;

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;


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
                Path dir = Paths.get(uploadDir, "expert-photos").toAbsolutePath().normalize();
                Files.createDirectories(dir);
                String original = photo.getOriginalFilename();
                String ext = (original != null && original.lastIndexOf('.') >= 0) ? original.substring(original.lastIndexOf('.')) : "";
                String filename = UUID.randomUUID().toString().replace("-", "") + ext;
                Path target = dir.resolve(filename);
                Files.copy(photo.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
                profile.setPhotoUrl("expert-photos/" + filename);
            } catch (Exception e) {
                log.error("专家照片上传失败", e);
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
        Map<String, Object> result = new HashMap<>();

        if (!checkToken(request)) {
            result.put("success", false);
            result.put("message", "未授权");
            return ResponseEntity.status(401).body(result);
        }

        Long expertId = getUserIdFromToken(request);
        ExpertProfile profile = expertProfileService.getProfile(expertId);

        if (profile == null) {
            // 档案不存在，但返回 200，前端只提示，不报错
            result.put("success", false);
            result.put("message", "档案不存在，请先创建");
            result.put("data", null);
            return ResponseEntity.ok(result);
        }

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
    public ResponseEntity<Map<String, Object>> listExperts() {
        try {
            List<ExpertInfoDTO> experts = expertProfileService.getAllExperts();
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", experts);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "获取专家列表失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取专家照片（二进制返回）
     * GET /api/expert/profile/{expertId}/photo
     */
    @GetMapping("/{expertId}/photo")
    public ResponseEntity<?> getExpertPhoto(@PathVariable Long expertId) {
        ExpertProfile profile = expertProfileService.getProfile(expertId);
        if (profile == null || profile.getPhotoUrl() == null || profile.getPhotoUrl().trim().isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        try {
            String photoUrl = profile.getPhotoUrl();
            Path filePath;
            if (photoUrl.startsWith("/uploads/")) {
                String relative = photoUrl.substring("/uploads/".length());
                filePath = Paths.get(uploadDir).resolve(relative).normalize().toAbsolutePath();
            } else {
                filePath = Paths.get(uploadDir).resolve(photoUrl).normalize().toAbsolutePath();
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
            log.error("读取专家照片失败", io);
            return ResponseEntity.internalServerError().body("Failed to read photo");
        }
    }

    /**
     * 单独更新专家照片
     * PATCH /api/expert/profile/photo
     */
    @PatchMapping(value = "/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> updatePhoto(
            HttpServletRequest request,
            @RequestPart("photo") MultipartFile photo) {
        
        if (!checkToken(request)) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "未授权"));
        }

        Long expertId = getUserIdFromToken(request);
        if (expertId == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "无法获取用户ID"));
        }

        if (photo == null || photo.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "照片文件不能为空"));
        }

        String newPhotoUrl = null;
        try {
            Path dir = Paths.get(uploadDir, "expert-photos").toAbsolutePath().normalize();
            Files.createDirectories(dir);
            String original = photo.getOriginalFilename();
            String ext = (original != null && original.lastIndexOf('.') >= 0) ? original.substring(original.lastIndexOf('.')) : "";
            String filename = UUID.randomUUID().toString().replace("-", "") + ext;
            Path target = dir.resolve(filename);
            Files.copy(photo.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            newPhotoUrl = "expert-photos/" + filename;
        } catch (Exception e) {
            log.error("更新专家照片上传失败", e);
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "文件上传失败: " + e.getMessage()));
        }

        boolean success = expertProfileService.updatePhotoUrl(expertId, newPhotoUrl);
        if (!success) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "更新照片失败，请确认专家档案已创建"));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "照片更新成功");
        result.put("photoUrl", newPhotoUrl);
        return ResponseEntity.ok(result);
    }
}