package com.example.agricultural_product.service.impl;

import com.example.agricultural_product.dto.UserApplicationDTO;
import com.example.agricultural_product.mapper.UserApplicationMapper;
import com.example.agricultural_product.pojo.UserApplication;
import com.example.agricultural_product.service.UserApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.util.Date;
import java.util.UUID;

@Service
public class UserApplicationServiceImpl implements UserApplicationService {

    @Autowired
    private UserApplicationMapper mapper;

    @Autowired
    private PasswordEncoder passwordEncoder; // 需要在配置类中配置这个Bean

    @Override
    @Transactional
    public boolean submitApplication(UserApplicationDTO applicationDTO, MultipartFile file) {
        try {
            // 1. 处理文件上传
            String filePath = null;
            if (file != null && !file.isEmpty()) {
                String uploadDir = System.getProperty("user.dir") + "/uploads/applications/";
                File dir = new File(uploadDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                String originalFilename = file.getOriginalFilename();
                String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                String newFileName = UUID.randomUUID().toString() + extension;
                
                File destFile = new File(uploadDir + newFileName);
                file.transferTo(destFile);
                filePath = "uploads/applications/" + newFileName;
            }

            // 2. 组装用户申请对象
            UserApplication application = new UserApplication();
            application.setUserName(applicationDTO.getUserName());
            application.setPassword(passwordEncoder.encode(applicationDTO.getPassword())); // 密码加密
            application.setRealName(applicationDTO.getName());
            application.setEmail(applicationDTO.getEmail());
            application.setReason(applicationDTO.getReason());
            application.setApplyRole(applicationDTO.getApplyRole());
            application.setAttachmentPath(filePath);
            application.setStatus("pending");
            application.setCreateTime(new Date());

            // 3. 保存到数据库
            return mapper.insert(application) > 0;

        } catch (Exception e) {
            throw new RuntimeException("提交申请失败: " + e.getMessage());
        }
    }
}
