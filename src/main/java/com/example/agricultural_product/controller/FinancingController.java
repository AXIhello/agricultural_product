package com.example.agricultural_product.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.agricultural_product.pojo.Financing;
import com.example.agricultural_product.pojo.FinancingOffer;
import com.example.agricultural_product.service.FinancingService;
import com.example.agricultural_product.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/financing")
public class FinancingController {

    @Autowired
    private FinancingService financingService;

    // 从 JWT token 获取 userId
    private Long getUserIdFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return JwtUtil.getUserId(token);
        }
        throw new RuntimeException("未提供有效的 JWT token");
    }

    /**
     * 农户创建融资申请（草稿状态）
     */
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createFinancing(
            HttpServletRequest request,
            @RequestBody Map<String, Object> params) {

        Long farmerId = getUserIdFromToken(request);
        BigDecimal amount = new BigDecimal(params.get("amount").toString());
        String purpose = (String) params.get("purpose");
        @SuppressWarnings("unchecked")
        List<Long> coApplicantIds = (List<Long>) params.get("coApplicantIds");

        Integer financingId = financingService.createFinancing(farmerId, amount, purpose, coApplicantIds);
        return ResponseEntity.ok(Map.of("success", true, "financingId", financingId));
    }

    /**
     * 农户提交融资申请（从草稿状态提交）
     */
    @PostMapping("/submit")
    public ResponseEntity<Boolean> submitFinancing(
            HttpServletRequest request,
            @RequestParam Integer financingId) {

        Long userId = getUserIdFromToken(request);
        return ResponseEntity.ok(financingService.submitFinancing(userId, financingId));
    }

    /**
     * 农户查看自己融资申请列表（分页）
     */
    @GetMapping("/my")
    public ResponseEntity<Page<Financing>> listUserFinancings(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        Long userId = getUserIdFromToken(request);
        return ResponseEntity.ok(financingService.listUserFinancings(userId, pageNum, pageSize));
    }

    /**
     * 查看融资申请详情（含参与农户信息）
     */
    @GetMapping("/detail")
    public ResponseEntity<Financing> getFinancingDetail(
            HttpServletRequest request,
            @RequestParam Integer financingId) {

        Long userId = getUserIdFromToken(request);
        return ResponseEntity.ok(financingService.getFinancingDetail(userId, financingId));
    }

    /**
     * 农户取消融资申请
     */
    @PostMapping("/cancel")
    public ResponseEntity<Boolean> cancelFinancing(
            HttpServletRequest request,
            @RequestParam Integer financingId) {

        Long userId = getUserIdFromToken(request);
        return ResponseEntity.ok(financingService.cancelFinancing(userId, financingId));
    }

    /**
     * 银行查看待审核的融资申请（分页）
     */
    @GetMapping("/pending")
    public ResponseEntity<Page<Financing>> listPendingFinancings(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        return ResponseEntity.ok(financingService.listPendingFinancings(pageNum, pageSize));
    }

    /**
     * 银行提交报价
     */
    @PostMapping("/offer/submit")
    public ResponseEntity<Boolean> submitOffer(
            HttpServletRequest request,
            @RequestBody Map<String, Object> params) {

        Long bankUserId = getUserIdFromToken(request);
        Integer financingId = ((Number) params.get("financingId")).intValue();
        BigDecimal offeredAmount = new BigDecimal(params.get("offeredAmount").toString());
        BigDecimal interestRate = new BigDecimal(params.get("interestRate").toString());
        String bankNotes = (String) params.get("bankNotes");

        boolean result = financingService.submitOffer(bankUserId, financingId, offeredAmount, interestRate, bankNotes);
        return ResponseEntity.ok(result);
    }

    /**
     * 查看某融资申请的所有银行报价
     */
    @GetMapping("/offers")
    public ResponseEntity<List<FinancingOffer>> listOffers(
            HttpServletRequest request,
            @RequestParam Integer financingId) {

        Long userId = getUserIdFromToken(request);
        return ResponseEntity.ok(financingService.listOffers(userId, financingId));
    }

    /**
     * 农户接受某个银行报价
     */
    @PostMapping("/offer/accept")
    public ResponseEntity<Boolean> acceptOffer(
            HttpServletRequest request,
            @RequestParam Integer offerId) {

        Long farmerId = getUserIdFromToken(request);
        return ResponseEntity.ok(financingService.acceptOffer(farmerId, offerId));
    }

    /**
     * 银行查看自己提交的报价列表
     */
    @GetMapping("/bank/offers")
    public ResponseEntity<Page<FinancingOffer>> listBankOffers(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        Long bankUserId = getUserIdFromToken(request);
        return ResponseEntity.ok(financingService.listBankOffers(bankUserId, pageNum, pageSize));
    }
}
