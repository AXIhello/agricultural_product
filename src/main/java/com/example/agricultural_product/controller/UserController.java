package com.example.agricultural_product.controller;

import com.example.agricultural_product.pojo.User;
import com.example.agricultural_product.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    //  登录接口
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> loginForm) {
        String userName = loginForm.get("user_name"); // 前端传的账号
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
        if (!user.getPassword().equals(password)) {
            response.put("success", false);
            response.put("message", "密码错误！");
            return response;
        }

        //测试用，只写了管理员
        if(role.equals("管理员")) role="admin";

        // 校验角色
        if (!user.getRole().equalsIgnoreCase(role)) {
            response.put("success", false);
            response.put("message", "角色不匹配！");
            return response;
        }

        response.put("success", true);
        response.put("message", "登录成功！");
        response.put("user", user);
        return response;
    }
}

