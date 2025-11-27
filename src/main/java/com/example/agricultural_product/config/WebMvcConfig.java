package com.example.agricultural_product.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Path root = Paths.get(uploadDir).toAbsolutePath().normalize();
        // String location = root.toUri().toString();
        // registry.addResourceHandler("/files/**")
        //         .addResourceLocations(location.endsWith("/") ? location : location + "/");
        // 1. 获取当前项目根目录的绝对路径
        String projectRoot = System.getProperty("user.dir");
        
        // 2. 处理路径：把 Windows 的反斜杠 \ 替换成正斜杠 /，并确保不以 / 结尾
        // 这一步对于 Windows 系统非常重要，否则 file: 协议可能无法识别
        projectRoot = projectRoot.replace("\\", "/");
        
        // 3. 拼接上传文件夹路径
        // 最终路径应该是类似于: D:/Code/AGRICULTURAL_PRODUCT/uploads/
        String uploadPath = projectRoot + "/uploads/";

        // 4. 打印路径到控制台，方便你排查（重启后请查看控制台输出）
        System.out.println("🔥 [静态资源映射] 物理路径: " + uploadPath);

        // 5. 配置映射
        // "file:" 告诉 Spring 这是文件系统路径
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath);
    }
}

