package com.example.agricultural_product.controller;

import com.example.agricultural_product.pojo.User;
import com.example.agricultural_product.service.UserService;
import com.example.agricultural_product.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private BCryptPasswordEncoder encoder;

    // ===== JWT 鉴权方法 =====
    private boolean checkToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return false;
        }
        String token = authHeader.substring(7);
        return JwtUtil.validateToken(token);
    }

    // ===== 从 Token 中获取用户ID =====
    private Long getUserIdFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7);
        return JwtUtil.getUserId(token);
    }

    /**
     * 通过 Token 获取当前用户信息
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getUserInfo(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();

        // 验证 token
        if (!checkToken(request)) {
            response.put("success", false);
            response.put("message", "未授权，请先登录！");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        // 从 token 中获取用户ID
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            response.put("success", false);
            response.put("message", "Token 解析失败！");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        // 查询用户信息
        User user = userService.findById(userId);
        if (user == null) {
            response.put("success", false);
            response.put("message", "用户不存在！");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        // 返回用户信息（不返回密码）
        response.put("success", true);
        response.put("message", "获取用户信息成功！");
        response.put("user", user);
        return ResponseEntity.ok(response);
    }

    //  登录接口（修改后，返回 token）
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginForm) {
        String userName = loginForm.get("userName");
        String password = loginForm.get("password");
        String role = loginForm.get("role");

        Map<String, Object> response = new HashMap<>();

        if (userName == null || password == null || role == null) {
            response.put("success", false);
            response.put("message", "用户名、密码和角色不能为空！");
            return ResponseEntity.badRequest().body(response);
        }

        // 查找用户
        User user = userService.findByUserName(userName);
        if (user == null) {
            response.put("success", false);
            response.put("message", "用户不存在！");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        // 校验密码
        if (!encoder.matches(password, user.getPassword())) {
            response.put("success", false);
            response.put("message", "密码错误！");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        // 校验角色
        if (!userName.equals("admin") && !user.getRole().equalsIgnoreCase(role)) {
            response.put("success", false);
            response.put("message", "角色不匹配！");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        //  登录成功，生成 JWT Token
        String token = JwtUtil.generateToken(user.getUserId(), user.getUserName());

        response.put("success", true);
        response.put("message", "登录成功！");
        response.put("user", user);
        response.put("token", token);

        return ResponseEntity.ok(response);
    }

    // 注册接口
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, String> registerForm) {
        String userName = registerForm.get("userName");
        String password = registerForm.get("password");
        String role = registerForm.get("role");
        String email = registerForm.get("email");

        Map<String, Object> response = new HashMap<>();

        if (userName == null || password == null || role == null) {
            response.put("success", false);
            response.put("message", "账号、密码和角色不能为空！");
            return response;
        }

        // 检查是否已存在
        User existingUser = userService.findByUserName(userName);
        if (existingUser != null) {
            response.put("success", false);
            response.put("message", "账号已存在！");
            return response;
        }

        // 创建新用户
        User newUser = new User();
        newUser.setUserName(userName);
        newUser.setPassword(encoder.encode(password));
        newUser.setName(userName);
        newUser.setRole(role);
        newUser.setEmail(email);

        int result = userService.registerUser(newUser);
        if (result > 0) {
            response.put("success", true);
            response.put("message", "注册成功！");
            response.put("user", newUser);
        } else {
            response.put("success", false);
            response.put("message", "注册失败，请稍后再试！");
        }

        return response;
    }

    // 根据用户名获取用户ID
    @GetMapping("/get-userid")
    public ResponseEntity<?> getUserId(@RequestParam String userName) {
        try {
            Long userId = userService.getUserIdByUserName(userName);
            if (userId != null) {
                return ResponseEntity.ok(Map.of("userId", userId));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "用户不存在"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "获取用户ID失败"));
        }
    }
}