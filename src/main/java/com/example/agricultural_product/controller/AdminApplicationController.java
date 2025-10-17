package com.example.agricultural_product.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.agricultural_product.common.Result;
import com.example.agricultural_product.pojo.UserApplication;
import com.example.agricultural_product.service.AdminApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/applications")
@RequiredArgsConstructor
public class AdminApplicationController {

    private final AdminApplicationService adminService;

    @GetMapping
    public List<UserApplication> getAllApplications() {
        return adminService.getAllApplications();
    }

    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id) {
        return adminService.approveApplication(id);
    }

    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Long id, @RequestParam String reason) {
        return adminService.rejectApplication(id, reason);
    }

    @GetMapping("/page")
    public Result<IPage<UserApplication>> getApplicationsPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        IPage<UserApplication> result = adminService.getApplicationsPage(page, size, status);
        return Result.success(result);
    }
}
