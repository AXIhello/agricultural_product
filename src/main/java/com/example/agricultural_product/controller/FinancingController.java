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
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7);
        return JwtUtil.getUserId(token);
    }

    /**
     * 农户直接提交融资申请（创建后立即提交）
     */
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createAndSubmitFinancing(
            HttpServletRequest request,
            @RequestBody Map<String, Object> params) {

        Long farmerId = getUserIdFromToken(request);
        BigDecimal amount = new BigDecimal(params.get("amount").toString());
        String purpose = (String) params.get("purpose");
        Integer term = Integer.parseInt(params.get("term").toString());


        // 修正类型转换问题
        @SuppressWarnings("unchecked")
        List<Object> coApplicantIdsRaw = (List<Object>) params.get("coApplicantIds");
        List<Long> coApplicantIds = null;
        if (coApplicantIdsRaw != null && !coApplicantIdsRaw.isEmpty()) {
            coApplicantIds = coApplicantIdsRaw.stream()
                    .map(id -> {
                        if (id instanceof Integer) {
                            return ((Integer) id).longValue();
                        } else if (id instanceof Long) {
                            return (Long) id;
                        } else {
                            return Long.parseLong(id.toString());
                        }
                    })
                    .toList();
        }

        // 创建融资申请
        Integer financingId = financingService.createFinancing(farmerId, amount, purpose, term, coApplicantIds);

        // 创建后立即提交
        financingService.submitFinancing(farmerId, financingId);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "financingId", financingId,
                "message", "融资申请已成功提交"
        ));
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
     * 取消融资申请
     */
    @PostMapping("/cancel")
    public ResponseEntity<Map<String, Object>> cancelFinancing(
            HttpServletRequest request,
            @RequestBody Map<String, Object> params) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "未授权"));
        }

        Integer financingId = (Integer) params.get("financingId");
        boolean result = financingService.cancelFinancing(userId, financingId);

        return ResponseEntity.ok(Map.of(
                "success", result,
                "message", result ? "取消成功" : "取消失败"
        ));
    }

    /**
     * 银行查看待审核的融资申请（分页）
     */
    @GetMapping("/submitted")
    public ResponseEntity<Page<Financing>> listSubmittedFinancings(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        return ResponseEntity.ok(financingService.listSubmittedFinancings(pageNum, pageSize));
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

    // 新增：农户拒绝某个银行报价
    @PostMapping("/offer/reject")
    public ResponseEntity<Boolean> rejectOffer(
            HttpServletRequest request,
            @RequestParam Integer offerId) {

        Long farmerId = getUserIdFromToken(request);
        return ResponseEntity.ok(financingService.rejectOffer(farmerId, offerId));
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

    /**
     * 银行拒绝融资申请
     */
    @PostMapping("/reject")
    public ResponseEntity<Boolean> rejectFinancing(
            HttpServletRequest request,
            @RequestBody Map<String, Object> params) {
        Long bankUserId = getUserIdFromToken(request);
        if (bankUserId == null) {
            return ResponseEntity.status(401).body(false);
        }

        Integer financingId = (Integer) params.get("financingId");
        String rejectReason = (String) params.get("rejectReason");

        boolean result = financingService.rejectFinancing(bankUserId, financingId, rejectReason);
        return ResponseEntity.ok(result);
    }

    /**
     * 标记拖欠还款（管理员或系统调用）
     */
    @PostMapping("/markOverdue")
    public ResponseEntity<Boolean> markOverdue(@RequestParam Integer financingId) {
        boolean result = financingService.markOverdue(financingId);
        return ResponseEntity.ok(result);
    }
}
