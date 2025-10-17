package com.example.agricultural_product.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.agricultural_product.pojo.UserApplication;

import java.util.List;

public interface AdminApplicationService {

    /**
     * 获取所有申请（不分页）
     */
    List<UserApplication> getAllApplications();

    /**
     * 分页查询申请记录，可按状态筛选
     * @param page 当前页码
     * @param size 每页大小
     * @param status 状态过滤（可选：pending, approved, rejected）
     */
    IPage<UserApplication> getApplicationsPage(int page, int size, String status);

    /**
     * 审核通过，并自动创建用户
     */
    String approveApplication(Long id);

    /**
     * 审核拒绝
     */
    String rejectApplication(Long id, String reason);
}
