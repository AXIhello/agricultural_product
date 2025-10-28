package com.example.agricultural_product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.agricultural_product.mapper.ExpertConsultationMapper;
import com.example.agricultural_product.mapper.ExpertWorkingSlotMapper;
import com.example.agricultural_product.pojo.ExpertConsultation;
import com.example.agricultural_product.pojo.ExpertWorkingSlot;
import com.example.agricultural_product.service.ExpertAppointmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ExpertAppointmentServiceImpl implements ExpertAppointmentService {

    private final ExpertWorkingSlotMapper slotMapper;
    private final ExpertConsultationMapper consultationMapper;

    public ExpertAppointmentServiceImpl(ExpertWorkingSlotMapper slotMapper, ExpertConsultationMapper consultationMapper) {
        this.slotMapper = slotMapper;
        this.consultationMapper = consultationMapper;
    }

    @Override
    @Transactional
    public boolean createSlots(Long expertId, List<ExpertWorkingSlot> slots) {
        if (expertId == null || slots == null || slots.isEmpty()) return false;
        for (ExpertWorkingSlot s : slots) {
            s.setExpertId(expertId);
            s.setStatus("open");
            s.setBookedCount(s.getBookedCount() == null ? 0 : s.getBookedCount());
            slotMapper.insert(s);
        }
        return true;
    }

    @Override
    public Page<ExpertWorkingSlot> listSlots(Long expertId, LocalDate from, LocalDate to, Integer pageNum, Integer pageSize) {
        Page<ExpertWorkingSlot> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ExpertWorkingSlot> qw = new LambdaQueryWrapper<ExpertWorkingSlot>()
                .eq(ExpertWorkingSlot::getExpertId, expertId)
                .ge(from != null, ExpertWorkingSlot::getWorkDate, from)
                .le(to != null, ExpertWorkingSlot::getWorkDate, to)
                .orderByAsc(ExpertWorkingSlot::getWorkDate).orderByAsc(ExpertWorkingSlot::getStartTime);
        return slotMapper.selectPage(page, qw);
    }

    @Override
    public Page<ExpertWorkingSlot> listOpenSlots(Long expertId, LocalDate from, LocalDate to, Integer pageNum, Integer pageSize) {
        Page<ExpertWorkingSlot> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ExpertWorkingSlot> qw = new LambdaQueryWrapper<ExpertWorkingSlot>()
                .eq(ExpertWorkingSlot::getExpertId, expertId)
                .eq(ExpertWorkingSlot::getStatus, "open")
                .apply("booked_count < capacity")
                .ge(from != null, ExpertWorkingSlot::getWorkDate, from)
                .le(to != null, ExpertWorkingSlot::getWorkDate, to)
                .orderByAsc(ExpertWorkingSlot::getWorkDate).orderByAsc(ExpertWorkingSlot::getStartTime);
        return slotMapper.selectPage(page, qw);
    }

    @Override
    @Transactional
    public Integer bookSlot(Long farmerId, Integer slotId) {
        ExpertWorkingSlot slot = slotMapper.selectById(slotId);
        if (slot == null || !"open".equals(slot.getStatus())) return null;

        // 原子增加已预约数，防止并发超卖
        UpdateWrapper<ExpertWorkingSlot> uw = new UpdateWrapper<>();
        uw.eq("slot_id", slotId)
          .eq("status", "open")
          .apply("booked_count < capacity")
          .setSql("booked_count = booked_count + 1");
        int changed = slotMapper.update(null, uw);
        if (changed != 1) return null; // 名额已满或已关闭

        // 写预约记录
        ExpertConsultation c = new ExpertConsultation();
        c.setFarmerId(farmerId);
        c.setExpertId(slot.getExpertId());
        c.setSlotId(slotId);
        c.setConsultationTime(LocalDateTime.of(slot.getWorkDate(), slot.getStartTime()));
        c.setStatus("scheduled");
        consultationMapper.insert(c);
        return c.getConsultationId();
    }

    @Override
    @Transactional
    public boolean cancelAppointment(Long farmerId, Integer consultationId) {
        ExpertConsultation c = consultationMapper.selectById(consultationId);
        if (c == null || !"scheduled".equals(c.getStatus())) return false;
        if (!c.getFarmerId().equals(farmerId)) return false;
        if (c.getConsultationTime().isBefore(LocalDateTime.now())) return false; // 已开始不可取消

        // 更新预约状态
        c.setStatus("cancelled");
        consultationMapper.updateById(c);

        // 原子回退名额
        UpdateWrapper<ExpertWorkingSlot> uw = new UpdateWrapper<>();
        uw.eq("slot_id", c.getSlotId())
          .apply("booked_count > 0")
          .setSql("booked_count = booked_count - 1");
        slotMapper.update(null, uw);
        return true;
    }

    @Override
    public Page<ExpertConsultation> listMyAppointments(Long farmerId, Integer pageNum, Integer pageSize) {
        Page<ExpertConsultation> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ExpertConsultation> qw = new LambdaQueryWrapper<ExpertConsultation>()
                .eq(ExpertConsultation::getFarmerId, farmerId)
                .orderByDesc(ExpertConsultation::getConsultationTime);
        return consultationMapper.selectPage(page, qw);
    }

    @Override
    public Page<ExpertConsultation> listExpertAppointments(Long expertId, LocalDate onDate, Integer pageNum, Integer pageSize) {
        Page<ExpertConsultation> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ExpertConsultation> qw = new LambdaQueryWrapper<ExpertConsultation>()
                .eq(ExpertConsultation::getExpertId, expertId);
        if (onDate != null) {
            // 仅当日
            LocalDateTime start = onDate.atStartOfDay();
            LocalDateTime end = onDate.plusDays(1).atStartOfDay().minusSeconds(1);
            qw.ge(ExpertConsultation::getConsultationTime, start)
              .le(ExpertConsultation::getConsultationTime, end);
        }
        qw.orderByAsc(ExpertConsultation::getConsultationTime);
        return consultationMapper.selectPage(page, qw);
    }
}