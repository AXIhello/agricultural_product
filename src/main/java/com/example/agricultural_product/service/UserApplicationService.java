package com.example.agricultural_product.service;

import com.example.agricultural_product.dto.UserApplicationDTO;
import com.example.agricultural_product.pojo.UserApplication;

import org.springframework.web.multipart.MultipartFile;

public interface UserApplicationService {
   boolean submitApplication(UserApplicationDTO applicationDTO, MultipartFile file);

   UserApplication findById(String id);
}
