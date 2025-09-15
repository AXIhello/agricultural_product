package com.example.agricultural_product.controller;

import com.example.agricultural_product.service.EmailService;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    @Resource
    private EmailService emailService;

    // 发送验证码（GET 可以保留 URL 参数方式）
    @GetMapping("/send-code")
    public ResponseEntity<Map<String, Object>> sendVerificationCode(@RequestParam String email) {
        Map<String, Object> response = new HashMap<>();
        try {
            String code = emailService.sendVerificationCode(email);
            response.put("success", true);
            response.put("message", "验证码已发送");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "验证码发送失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 验证验证码（接收 JSON）
    @PostMapping("/verify-code")
    public ResponseEntity<Map<String, Object>> verifyCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");
        Map<String, Object> response = new HashMap<>();
        if (email == null || code == null) {
            response.put("success", false);
            response.put("message", "缺少邮箱或验证码参数");
            return ResponseEntity.badRequest().body(response);
        }

        boolean isValid = emailService.verifyCode(email, code);
        if (isValid) {
            response.put("success", true);
            response.put("message", "验证成功");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "验证失败，验证码错误或已过期");
            return ResponseEntity.badRequest().body(response);
        }
    }
}
