package com.example.agricultural_product.controller;

import com.example.agricultural_product.pojo.User;
import com.example.agricultural_product.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private BCryptPasswordEncoder encoder; // 改为注入方式

    //  登录接口
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> loginForm) {
        String userName = loginForm.get("userName"); // 前端传的账号
        String password = loginForm.get("password");
        String role = loginForm.get("role"); // admin / buyer / farmer

        Map<String, Object> response = new HashMap<>();

        if (userName == null || password == null || role == null) {
            response.put("success", false);
            response.put("message", "用户名、密码和角色不能为空！");
            return response;
        }

        // 查找用户
        User user = userService.findByUserName(userName);
        if (user == null) {
            response.put("success", false);
            response.put("message", "用户不存在！");
            return response;
        }

        // 校验密码
        if (!encoder.matches(password, user.getPassword())) {
            response.put("success", false);
            response.put("message", "密码错误！");
            return response;
        }

        // 校验角色
        if (!userName.equals("admin")&&!user.getRole().equalsIgnoreCase(role)) {
            response.put("success", false);
            response.put("message", "角色不匹配！");
            return response;
        }

        response.put("success", true);
        response.put("message", "登录成功！");
        response.put("user", user);
        return response;
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, String> registerForm) {
        String userName = registerForm.get("userName"); // 账号
        String password = registerForm.get("password");
        String role = registerForm.get("role"); // admin / buyer / farmer
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
        // 使用 encoder 加密密码
        newUser.setPassword(encoder.encode(password));
        newUser.setName(userName); // 默认和账号一样
        newUser.setRole(role);
        newUser.setEmail(email); // 设置邮箱

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

}

