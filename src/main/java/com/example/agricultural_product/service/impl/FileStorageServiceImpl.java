package com.example.agricultural_product.service.impl;

import com.example.agricultural_product.service.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service // 标记这是一个 Spring 管理的服务实现类
public class FileStorageServiceImpl implements FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageServiceImpl.class);

    private final Path fileStorageLocation;
    private final String storageDirectory = "uploads/expert-photos"; // 将目录名定义为常量

    public FileStorageServiceImpl() {
        // 定义文件存储的根目录
        this.fileStorageLocation = Paths.get(storageDirectory).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
            logger.info("文件存储目录已成功创建或已存在: {}", this.fileStorageLocation);
        } catch (Exception ex) {
            logger.error("无法创建文件存储目录", ex);
            throw new RuntimeException("无法创建用于存储上传文件的目录！", ex);
        }
    }

    @Override
    public String storeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传的文件不能为空");
        }

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            if (originalFileName.contains("..")) {
                throw new RuntimeException("文件名包含无效的路径序列: " + originalFileName);
            }

            String fileExtension = "";
            int dotIndex = originalFileName.lastIndexOf('.');
            if (dotIndex >= 0) {
                fileExtension = originalFileName.substring(dotIndex);
            }
            String newFileName = UUID.randomUUID().toString() + fileExtension;

            Path targetLocation = this.fileStorageLocation.resolve(newFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            logger.info("文件已成功存储: {}", targetLocation);

            // 返回可访问的URL路径
            return "/" + storageDirectory + "/" + newFileName;

        } catch (IOException ex) {
            logger.error("文件存储失败: {}", originalFileName, ex);
            throw new RuntimeException("无法存储文件 " + originalFileName + "。请再试一次！", ex);
        }
    }

    @Override
    public boolean deleteFile(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            return false;
        }

        try {
            // 从相对URL路径中提取文件名
            String filename = StringUtils.getFilename(filePath);
            if (filename == null) {
                logger.warn("无法从路径中提取文件名: {}", filePath);
                return false;
            }

            Path targetLocation = this.fileStorageLocation.resolve(filename).normalize();

            // 安全性检查：确保要删除的文件确实在我们的存储目录下
            if (!targetLocation.startsWith(this.fileStorageLocation)) {
                logger.error("潜在的路径遍历删除攻击: {}", filePath);
                return false;
            }

            if (Files.exists(targetLocation)) {
                Files.delete(targetLocation);
                logger.info("文件已成功删除: {}", targetLocation);
                return true;
            } else {
                logger.warn("尝试删除一个不存在的文件: {}", targetLocation);
                return false;
            }
        } catch (IOException e) {
            logger.error("删除文件时发生错误: {}", filePath, e);
            return false;
        }
    }
}