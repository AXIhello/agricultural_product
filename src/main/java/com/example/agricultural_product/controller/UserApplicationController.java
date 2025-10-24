package com.example.agricultural_product.controller;

import com.example.agricultural_product.dto.UserApplicationDTO;
import com.example.agricultural_product.common.Result;
import com.example.agricultural_product.service.UserApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/apply")
public class UserApplicationController {

    @Autowired
    private UserApplicationService service;

    @PostMapping(value = "/submit", consumes = {"multipart/form-data"})
    public Result<?> submitApplication(
            @RequestParam("userName") String userName,
            @RequestParam("password") String password,
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("reason") String reason,
            @RequestParam("applyRole") String applyRole,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        try {
            UserApplicationDTO dto = new UserApplicationDTO();
            dto.setUserName(userName);
            dto.setPassword(password);
            dto.setName(name);
            dto.setEmail(email);
            dto.setReason(reason);
            dto.setApplyRole(applyRole);

            boolean success = service.submitApplication(dto, file);
            return success ? Result.success("申请已提交，请等待管理员审核")
                    : Result.error("申请提交失败");
        } catch (Exception e) {
            return Result.error("申请提交异常: " + e.getMessage());
        }
    }

}

