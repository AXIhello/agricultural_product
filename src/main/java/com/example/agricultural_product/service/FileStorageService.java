package com.example.agricultural_product.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储服务接口
 * 定义了文件存储和管理相关操作的契约
 */
public interface FileStorageService {

    /**
     * 存储上传的文件。
     *
     * @param file 从客户端上传的 MultipartFile 对象
     * @return 存储后文件的可访问相对URL路径。
     */
    String storeFile(MultipartFile file);

    /**
     * 删除指定路径的文件。
     *
     * @param filePath 文件的相对URL路径。
     * @return 如果删除成功，则返回 true；否则返回 false
     */
    boolean deleteFile(String filePath);

    
}