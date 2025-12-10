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

import java.math.BigDecimal;
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

    // ===== 从 Token 中获取用户角色 =====
    private String getUserRoleFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7);
        return JwtUtil.getRole(token);
    }

    private boolean isAdmin(HttpServletRequest request) {
        if (!checkToken(request)) {
            return false;
        }
        String role = getUserRoleFromToken(request);
        return role != null && "admin".equalsIgnoreCase(role);
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

        user.setPassword(null);

        // 返回用户信息
        response.put("success", true);
        response.put("message", "获取用户信息成功！");
        response.put("user", user);
        return ResponseEntity.ok(response);
    }
    // 通过用户ID获取用户信息（公开接口，已去除密码）
    @GetMapping("/info/{userId}")
    public ResponseEntity<Map<String, Object>> getUserInfoById(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();

        User user = userService.findById(userId);
        if (user == null) {
            response.put("success", false);
            response.put("message", "用户不存在！");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        // 隐藏敏感信息
        user.setPassword(null);

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
        String token = JwtUtil.generateToken(user.getUserId(), user.getUserName(),user.getRole());

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
        // 设置初始信用分为70
        newUser.setCreditScore(70);
        // 初始化其他字段
        newUser.setHistoricalSuccessRate(BigDecimal.valueOf(0.0));
        newUser.setAverageFinancingAmount(BigDecimal.valueOf(0.0));
        newUser.setFinancingActivityLevel("low");

        int result = userService.registerUser(newUser);
        if (result > 0) {
            response.put("success", true);
            response.put("message", "注册成功！");
            // 不返回密码
            newUser.setPassword(null);
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
    /**
     * 更新用户的地区、信用分等基础资料
     */
    @PostMapping("/update/region")
    public ResponseEntity<Boolean> updateRegion(
            HttpServletRequest request,
            @RequestBody Map<String, Object> params) {

        Long userId = getUserIdFromToken(request);
        String region = (String) params.get("region");

        boolean result = userService.updateRegion(userId, region);
        return ResponseEntity.ok(result);
    }
    @PostMapping("/update/credit")
    public ResponseEntity<Boolean> updateCreditScore(
            HttpServletRequest request,
            @RequestBody Map<String, Object> params) {

        Long userId = getUserIdFromToken(request);
        Integer creditScore = (Integer) params.get("creditScore");

        boolean result = userService.updateCreditScore(userId, creditScore);
        return ResponseEntity.ok(result);
    }

    /**
     * 用户自行更新基础资料（昵称、地区等）
     */
    @PostMapping("/update/profile")
    public ResponseEntity<Boolean> updateProfile(
            HttpServletRequest request,
            @RequestBody User userParams) { // 这里直接接收 User 对象或 Map 都可以

        Long userId = getUserIdFromToken(request);
        
        // 构造要更新的数据
        User userToUpdate = new User();
        userToUpdate.setUserId(userId);
        
        // 允许修改的字段
        if (userParams.getName() != null) {
            userToUpdate.setName(userParams.getName());
        }
        if (userParams.getRegion() != null) {
            userToUpdate.setRegion(userParams.getRegion());
        }
        
        // 调用 Service 更新（MyBatis Plus 的 updateById 会自动忽略 null 字段）
        boolean result = userService.updateById(userToUpdate);
        
        return ResponseEntity.ok(result);
    }


    /**
     * 管理员分页查看所有用户列表（不返回密码）
     */
    @GetMapping("/admin/list")
    public ResponseEntity<?> listAllUsers(HttpServletRequest request,
                                          @RequestParam(defaultValue = "1") int pageNum,
                                          @RequestParam(defaultValue = "10") int pageSize) {
        if (!isAdmin(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "message", "无权限，只有管理员可以访问该接口"));
        }
        // 这里假设 UserService 提供分页方法，如未提供，可以先返回全部列表
        // List<User> users = userService.findAllUsers(pageNum, pageSize);
        // 为保持兼容性，这里先简单返回所有用户列表
        java.util.List<User> users = userService.findAll();
        users.forEach(u -> u.setPassword(null));
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("message", "获取用户列表成功");
        resp.put("users", users);
        return ResponseEntity.ok(resp);
    }

    /**
     * 管理员根据用户ID查看单个用户详情（不返回密码）
     */
    @GetMapping("/admin/{userId}")
    public ResponseEntity<?> getUserByAdmin(HttpServletRequest request, @PathVariable Long userId) {
        if (!isAdmin(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "message", "无权限，只有管理员可以访问该接口"));
        }
        User user = userService.findById(userId);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "用户不存在"));
        }
        user.setPassword(null);
        return ResponseEntity.ok(Map.of("success", true, "user", user));
    }

    /**
     * 管理员更新用户基础信息（不处理密码修改）
     */
    @PutMapping("/admin/{userId}")
    public ResponseEntity<?> updateUserByAdmin(HttpServletRequest request,
                                               @PathVariable Long userId,
                                               @RequestBody Map<String, Object> body) {
        if (!isAdmin(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "message", "无权限，只有管理员可以访问该接口"));
        }
        User user = userService.findById(userId);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "用户不存在"));
        }
        if (body.containsKey("name")) {
            user.setName((String) body.get("name"));
        }
        if (body.containsKey("email")) {
            user.setEmail((String) body.get("email"));
        }
        if (body.containsKey("role")) {
            user.setRole((String) body.get("role"));
        }
        if (body.containsKey("region")) {
            user.setRegion((String) body.get("region"));
        }
        if (body.containsKey("creditScore")) {
            Object cs = body.get("creditScore");
            if (cs instanceof Number) {
                user.setCreditScore(((Number) cs).intValue());
            }
        }
        // 其他字段根据需要补充
        userService.updateUser(user);
        user.setPassword(null);
        return ResponseEntity.ok(Map.of("success", true, "message", "更新成功", "user", user));
    }

    /**
     * 管理员删除用户
     */
    @DeleteMapping("/admin/{userId}")
    public ResponseEntity<?> deleteUserByAdmin(HttpServletRequest request, @PathVariable Long userId) {
        if (!isAdmin(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "message", "无权限，只有管理员可以访问该接口"));
        }
        boolean ok = userService.deleteById(userId);
        if (!ok) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "用户不存在或删除失败"));
        }
        return ResponseEntity.ok(Map.of("success", true, "message", "删除成功"));
    }

    /**
     * 用户修改密码（从 Token 解析 userId，无额外请求 DTO）
     */
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(HttpServletRequest request,
                                                              @RequestBody Map<String, String> body) {
        Map<String, Object> response = new HashMap<>();
        if (!checkToken(request)) {
            response.put("success", false);
            response.put("message", "未授权，请先登录！");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            response.put("success", false);
            response.put("message", "Token 解析失败！");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        String newPassword = body.get("newPassword");
        if (newPassword == null || newPassword.isEmpty()) {
            response.put("success", false);
            response.put("message", "新密码不能为空！");
            return ResponseEntity.badRequest().body(response);
        }

        User user = userService.findById(userId);
        if (user == null) {
            response.put("success", false);
            response.put("message", "用户不存在！");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        user.setPassword(encoder.encode(newPassword));
        userService.updateById(user);

        response.put("success", true);
        response.put("message", "密码修改成功！");
        return ResponseEntity.ok(response);
    }
}