package com.example.agricultural_product.service;

import com.example.agricultural_product.dto.UserApplicationDTO;
import org.springframework.web.multipart.MultipartFile;

public interface UserApplicationService {
    boolean submitApplication(UserApplicationDTO applicationDTO, MultipartFile file);
}
