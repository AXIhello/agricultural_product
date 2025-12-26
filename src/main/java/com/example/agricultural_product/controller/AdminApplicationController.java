package com.example.agricultural_product.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.agricultural_product.common.Result;
import com.example.agricultural_product.pojo.UserApplication;
import com.example.agricultural_product.service.AdminApplicationService;
import com.example.agricultural_product.service.UserApplicationService;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.StringUtils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/applications")
@RequiredArgsConstructor
public class AdminApplicationController {

    private static final Logger log = LoggerFactory.getLogger(AdminApplicationController.class);

    @Autowired
    private UserApplicationService userApplicationService; 

    private final AdminApplicationService adminService;
   
    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    private Path absoluteUploadPath;

    @PostConstruct // 在依赖注入完成后执行
    public void init() {
        this.absoluteUploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        // 确保上传目录存在
        try {
            Files.createDirectories(this.absoluteUploadPath);
        } catch (IOException e) {
            log.error("无法创建文件上传目录: " + this.absoluteUploadPath, e);
            // 考虑在此处抛出运行时异常，阻止应用启动
        }
    }

    @GetMapping
    public List<UserApplication> getAllApplications() {
        return adminService.getAllApplications();
    }

    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id) {
        return adminService.approveApplication(id);
    }

    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Long id, @RequestParam String reason) {
        return adminService.rejectApplication(id, reason);
    }

    @GetMapping("/page")
    public Result<IPage<UserApplication>> getApplicationsPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        IPage<UserApplication> result = adminService.getApplicationsPage(page, size, status);
        return Result.success(result);
    }

    /**
     * 获取指定申请的附件图片（二进制返回）
     * GET /api/admin/applications/{applicationId}/attachment
     *
     * @param applicationId 申请的ID
     * @param ifNoneMatch   用于协商缓存的 ETag
     * @return ResponseEntity 包含图片数据或 304 Not Modified
     */
    @GetMapping("/{applicationId}/attachment")
    public ResponseEntity<?> getApplicationAttachment(
            @PathVariable String applicationId, // applicationId 可能是字符串类型
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {

        log.info("尝试获取申请 {} 的附件", applicationId);

        UserApplication application = userApplicationService.findById(applicationId); // 调用你的服务层查找申请
        if (application == null || !StringUtils.hasText(application.getAttachmentPath())) {
            log.warn("申请 {} 不存在或无附件路径", applicationId);
            return ResponseEntity.notFound().build();
        }

        try {
            String attachmentPathInDb = application.getAttachmentPath();
            Path filePath;

            String effectiveAttachmentPath;
            // 如果数据库中的路径以 uploadDir (例如 "uploads/") 开头，则移除它
            if (attachmentPathInDb.startsWith(uploadDir + "/")) {
                effectiveAttachmentPath = attachmentPathInDb.substring((uploadDir + "/").length());
                log.debug("数据库路径 '{}' 移除 '{}' 前缀后为 '{}'", attachmentPathInDb, uploadDir + "/", effectiveAttachmentPath);
            } else {
                effectiveAttachmentPath = attachmentPathInDb;
                log.debug("数据库路径 '{}' 没有 '{}' 前缀，直接使用。", attachmentPathInDb, uploadDir + "/");
            }

            filePath = this.absoluteUploadPath.resolve(effectiveAttachmentPath).normalize();

            // 严格检查解析后的路径是否仍在上传目录下，防止路径遍历攻击
            if (!filePath.startsWith(this.absoluteUploadPath)) {
                log.warn("检测到路径遍历尝试，申请 {} 的附件路径 {} 超出允许范围", applicationId, attachmentPathInDb);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // 403 Forbidden
            }

            if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
                log.warn("申请 {} 的附件文件 {} 不存在或不是常规文件", applicationId, filePath);
                return ResponseEntity.notFound().build();
            }

            // 生成 ETag（基于文件路径和最后修改时间）
            long lastModifiedMillis = Files.getLastModifiedTime(filePath).toMillis();
            String etag = "\"" + attachmentPathInDb.hashCode() + "-" + lastModifiedMillis + "\"";

            // 检查 ETag 是否匹配，如果匹配则返回 304 Not Modified
            if (etag.equals(ifNoneMatch)) {
                log.info("申请 {} 的附件命中缓存 (ETag: {})", applicationId, etag);
                return ResponseEntity.status(304).build();
            }

            byte[] bytes = Files.readAllBytes(filePath);
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;

            // 规范的 Last-Modified 日期格式
            ZonedDateTime lastModifiedDateTime = Instant.ofEpochMilli(lastModifiedMillis).atZone(ZoneId.of("GMT"));
            String httpLastModified = DateTimeFormatter.RFC_1123_DATE_TIME.format(lastModifiedDateTime);

            log.info("成功获取申请 {} 的附件，大小：{} 字节，Content-Type: {}", applicationId, bytes.length, contentType);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CACHE_CONTROL, "max-age=3600, public") // 缓存1小时
                    .header(HttpHeaders.ETAG, etag)
                    .header(HttpHeaders.LAST_MODIFIED, httpLastModified) // 使用规范的日期格式
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(new ByteArrayResource(bytes));

        } catch (IOException io) {
            log.error("读取申请 {} 的附件照片失败: {}", applicationId, io.getMessage(), io);
            return ResponseEntity.internalServerError().body("Failed to read attachment");
        }
    }
}
