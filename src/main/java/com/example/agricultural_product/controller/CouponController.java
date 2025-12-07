package com.example.agricultural_product.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.agricultural_product.dto.coupon.*;
import com.example.agricultural_product.entity.CouponTemplate;
import com.example.agricultural_product.entity.UserCoupon;
import com.example.agricultural_product.service.CouponService;
import com.example.agricultural_product.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 优惠券控制器
 */
@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {
    
    private final CouponService couponService;
    
    /**
     * 创建优惠券模板（管理员）
     * POST /api/coupons/templates
     */
    @PostMapping("/templates")
    public ResponseEntity<?> createTemplate(@Validated @RequestBody CreateCouponTemplateDTO dto,
                                           HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization").substring(7);
            String role = JwtUtil.getRole(token);
            
            // 验证管理员权限
            if (!"admin".equals(role)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "权限不足，仅管理员可创建优惠券模板");
                return ResponseEntity.status(403).body(response);
            }
            
            Long operatorId = JwtUtil.getUserId(token);
            CouponTemplate template = couponService.createTemplate(dto, operatorId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "优惠券模板创建成功");
            response.put("data", template);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 查询优惠券模板列表
     * 管理员可查看所有状态，买家用户只能查看active状态
     * GET /api/coupons/templates?status=active&pageNum=1&pageSize=10
     */
    @GetMapping("/templates")
    public ResponseEntity<?> getTemplates(@RequestParam(required = false, defaultValue = "all") String status,
                                         @RequestParam(defaultValue = "1") Integer pageNum,
                                         @RequestParam(defaultValue = "10") Integer pageSize,
                                         HttpServletRequest request) {
        try {
            // 获取用户角色
            String role = "buyer"; // 默认为买家
            try {
                String token = request.getHeader("Authorization").substring(7);
                role = JwtUtil.getRole(token);
            } catch (Exception e) {
                // 未登录或token无效，默认为买家
            }
            
            // 买家用户只能查看active状态的模板
            if (!"admin".equals(role) && !"active".equals(status)) {
                status = "active";
            }
            
            Page<CouponTemplate> page = couponService.getTemplates(status, pageNum, pageSize);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", page);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 更新优惠券模板状态（管理员）
     * PUT /api/coupons/templates/{templateId}/status
     */
    @PutMapping("/templates/{templateId}/status")
    public ResponseEntity<?> updateTemplateStatus(@PathVariable Integer templateId,
                                                  @RequestParam String status,
                                                  HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization").substring(7);
            String role = JwtUtil.getRole(token);
            
            // 验证管理员权限
            if (!"admin".equals(role)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "权限不足，仅管理员可修改优惠券状态");
                return ResponseEntity.status(403).body(response);
            }
            
            couponService.updateTemplateStatus(templateId, status);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "状态更新成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 用户领取优惠券
     * POST /api/coupons/receive/{templateId}
     */
    @PostMapping("/receive/{templateId}")
    public ResponseEntity<?> receiveCoupon(@PathVariable Integer templateId,
                                          HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization").substring(7);
            Long userId = JwtUtil.getUserId(token);
            UserCoupon userCoupon = couponService.receiveCoupon(userId, templateId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "领取成功");
            response.put("data", userCoupon);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 批量发放优惠券（管理员）
     * POST /api/coupons/batch-issue
     */
    @PostMapping("/batch-issue")
    public ResponseEntity<?> batchIssueCoupons(@Validated @RequestBody BatchIssueCouponDTO dto,
                                              HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization").substring(7);
            String role = JwtUtil.getRole(token);
            
            // 验证管理员权限
            if (!"admin".equals(role)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "权限不足，仅管理员可批量发放优惠券");
                return ResponseEntity.status(403).body(response);
            }
            
            Long operatorId = JwtUtil.getUserId(token);
            couponService.batchIssueCoupons(dto, operatorId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "批量发放成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 查询我的优惠券列表
     * GET /api/coupons/my?status=unused&pageNum=1&pageSize=10
     */
    @GetMapping("/my")
    public ResponseEntity<?> getMyCoupons(@RequestParam(required = false, defaultValue = "all") String status,
                                         @RequestParam(defaultValue = "1") Integer pageNum,
                                         @RequestParam(defaultValue = "10") Integer pageSize,
                                         HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization").substring(7);
            Long userId = JwtUtil.getUserId(token);
            Page<CouponDTO> page = couponService.getUserCoupons(userId, status, pageNum, pageSize);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", page);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 查询订单可用优惠券
     * POST /api/coupons/available
     * Body: { "orderAmount": 150.00, "productIds": [1, 2, 3] }
     */
    @PostMapping("/available")
    public ResponseEntity<?> getAvailableCoupons(@RequestBody Map<String, Object> params,
                                                 HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization").substring(7);
            Long userId = JwtUtil.getUserId(token);
            
            BigDecimal orderAmount = new BigDecimal(params.get("orderAmount").toString());
            @SuppressWarnings("unchecked")
            List<Integer> productIds = (List<Integer>) params.get("productIds");
            
            List<AvailableCouponDTO> coupons = couponService.getAvailableCoupons(userId, orderAmount, productIds);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", coupons);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 锁定优惠券（下单时调用）
     * POST /api/coupons/{userCouponId}/lock
     */
    @PostMapping("/{userCouponId}/lock")
    public ResponseEntity<?> lockCoupon(@PathVariable Long userCouponId,
                                       HttpServletRequest request) {
        try {
            couponService.lockCoupon(userCouponId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "优惠券已锁定");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 解锁优惠券（取消订单时调用）
     * POST /api/coupons/{userCouponId}/unlock
     */
    @PostMapping("/{userCouponId}/unlock")
    public ResponseEntity<?> unlockCoupon(@PathVariable Long userCouponId,
                                         HttpServletRequest request) {
        try {
            couponService.unlockCoupon(userCouponId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "优惠券已解锁");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 优惠券统计信息
     * GET /api/coupons/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getCouponStatistics(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization").substring(7);
            Long userId = JwtUtil.getUserId(token);
            
            // 获取各状态优惠券数量
            Page<CouponDTO> unusedPage = couponService.getUserCoupons(userId, "unused", 1, 1);
            Page<CouponDTO> usedPage = couponService.getUserCoupons(userId, "used", 1, 1);
            Page<CouponDTO> expiredPage = couponService.getUserCoupons(userId, "expired", 1, 1);
            
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("unused", unusedPage.getTotal());
            statistics.put("used", usedPage.getTotal());
            statistics.put("expired", expiredPage.getTotal());
            statistics.put("total", unusedPage.getTotal() + usedPage.getTotal() + expiredPage.getTotal());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", statistics);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
