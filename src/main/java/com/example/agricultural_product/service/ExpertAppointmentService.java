package com.example.agricultural_product.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.agricultural_product.pojo.ExpertConsultation;
import com.example.agricultural_product.pojo.ExpertWorkingSlot;

import java.time.LocalDate;
import java.util.List;

public interface ExpertAppointmentService {
    // 专家创建/维护工作时间段
    boolean createSlots(Long expertId, List<ExpertWorkingSlot> slots);
    Page<ExpertWorkingSlot> listSlots(Long expertId, LocalDate from, LocalDate to, Integer pageNum, Integer pageSize);

    // 农户浏览可预约的时间段
    Page<ExpertWorkingSlot> listOpenSlots(Long expertId, LocalDate from, LocalDate to, Integer pageNum, Integer pageSize);

    // 预约与取消
    Integer bookSlot(Long farmerId, Integer slotId);
    boolean cancelAppointment(Long farmerId, Integer consultationId);

    // 列表
    Page<ExpertConsultation> listMyAppointments(Long farmerId, Integer pageNum, Integer pageSize);
    Page<ExpertConsultation> listExpertAppointments(Long expertId, LocalDate onDate, Integer pageNum, Integer pageSize);
}