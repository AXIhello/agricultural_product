package com.example.agricultural_product.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.agricultural_product.pojo.ExpertConsultation;
import com.example.agricultural_product.pojo.ExpertWorkingSlot;
import com.example.agricultural_product.service.ExpertAppointmentService;
import com.example.agricultural_product.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/expert-appointments")
public class ExpertAppointmentController {

    private final ExpertAppointmentService service;

    public ExpertAppointmentController(ExpertAppointmentService service) {
        this.service = service;
    }

    private Long getUserIdFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        String token = authHeader.substring(7);
        return JwtUtil.getUserId(token); // 参见：com.example.agricultural_product.utils.JwtUtil
    }

    // 专家创建工作时间段
    @PostMapping("/slots")
    public ResponseEntity<Boolean> createSlots(HttpServletRequest request, @RequestBody List<ExpertWorkingSlot> slots) {
        Long expertId = getUserIdFromToken(request);
        return ResponseEntity.ok(service.createSlots(expertId, slots));
    }

    // 专家查看自己的时间段
    @GetMapping("/slots")
    public ResponseEntity<Page<ExpertWorkingSlot>> listSlots(
            HttpServletRequest request,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long expertId = getUserIdFromToken(request);
        LocalDate f = (from == null || from.isEmpty()) ? null : LocalDate.parse(from);
        LocalDate t = (to == null || to.isEmpty()) ? null : LocalDate.parse(to);
        return ResponseEntity.ok(service.listSlots(expertId, f, t, pageNum, pageSize));
    }

    // 农户查看某专家开放时间段（带余量）
    @GetMapping("/slots/open")
    public ResponseEntity<Page<ExpertWorkingSlot>> listOpenSlots(
            @RequestParam Long expertId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        LocalDate f = (from == null || from.isEmpty()) ? null : LocalDate.parse(from);
        LocalDate t = (to == null || to.isEmpty()) ? null : LocalDate.parse(to);
        return ResponseEntity.ok(service.listOpenSlots(expertId, f, t, pageNum, pageSize));
    }

    // 农户预约
    @PostMapping("/book")
    public ResponseEntity<Map<String, Object>> book(HttpServletRequest request, @RequestParam Integer slotId) {
        Long farmerId = getUserIdFromToken(request);
        Integer consultationId = service.bookSlot(farmerId, slotId);
        boolean success = consultationId != null;
        return ResponseEntity.ok(Map.of("success", success, "consultationId", consultationId));
    }

    // 农户取消预约
    @PostMapping("/cancel")
    public ResponseEntity<Boolean> cancel(HttpServletRequest request, @RequestParam Integer consultationId) {
        Long farmerId = getUserIdFromToken(request);
        return ResponseEntity.ok(service.cancelAppointment(farmerId, consultationId));
    }

    // 农户查看我的预约
    @GetMapping("/my")
    public ResponseEntity<Page<ExpertConsultation>> myAppointments(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long farmerId = getUserIdFromToken(request);
        return ResponseEntity.ok(service.listMyAppointments(farmerId, pageNum, pageSize));
    }

    // 专家查看当天预约排期
    @GetMapping("/expert/day")
    public ResponseEntity<Page<ExpertConsultation>> expertDay(
            HttpServletRequest request,
            @RequestParam String date,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long expertId = getUserIdFromToken(request);
        return ResponseEntity.ok(service.listExpertAppointments(expertId, LocalDate.parse(date), pageNum, pageSize));
    }
}