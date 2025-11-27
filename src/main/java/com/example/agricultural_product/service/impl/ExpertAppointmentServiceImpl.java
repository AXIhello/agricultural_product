package com.example.agricultural_product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.agricultural_product.mapper.ExpertConsultationMapper;
import com.example.agricultural_product.mapper.ExpertWorkingSlotMapper;
import com.example.agricultural_product.mapper.UserMapper;
import com.example.agricultural_product.pojo.ExpertConsultation;
import com.example.agricultural_product.pojo.ExpertWorkingSlot;
import com.example.agricultural_product.service.ExpertAppointmentService;
import com.example.agricultural_product.utils.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ExpertAppointmentServiceImpl implements ExpertAppointmentService {

    private final ExpertWorkingSlotMapper slotMapper;
    private final ExpertConsultationMapper consultationMapper;
    private final com.example.agricultural_product.mapper.UserMapper userMapper; 

    public ExpertAppointmentServiceImpl(ExpertWorkingSlotMapper slotMapper, ExpertConsultationMapper consultationMapper, com.example.agricultural_product.mapper.UserMapper userMapper) {
        this.slotMapper = slotMapper;
        this.consultationMapper = consultationMapper;
        this.userMapper = userMapper;
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
        
        //检查该农户是否已经预约过该时间段（排除已取消的）
        LambdaQueryWrapper<ExpertConsultation> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(ExpertConsultation::getFarmerId, farmerId)
                    .eq(ExpertConsultation::getSlotId, slotId)
                    .ne(ExpertConsultation::getStatus, "cancelled"); // 关键：排除已取消的记录
        
        Long count = consultationMapper.selectCount(checkWrapper);
        if (count > 0) {
            System.out.println(" 预约失败：检测到重复预约 (FarmerId=" + farmerId + ", SlotId=" + slotId + ")");
            // 返回 null 表示失败（或者你可以抛出一个自定义异常，让Controller捕获并返回具体错误信息）
            throw new BusinessException("您已预约过该专家的同一时间段，不可重复预约！"); 
        }

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

        // 查出预约记录
        Page<ExpertConsultation> result = consultationMapper.selectPage(page, qw);

        // 🔥 2. 遍历每一条记录，填入专家名字
        if (result.getRecords() != null) {
            result.getRecords().forEach(item -> {
                Long expertId = item.getExpertId();
                
                // 去用户表查名字
                // 假设你的用户实体叫 User，获取名字的方法叫 getUserName() 或 getName()
                var user = userMapper.selectById(expertId);
                
                if (user != null) {
                    String name = userMapper.getUserName(item.getExpertId()); // 先把名字取出来放到变量里
                    item.setExpertName(name); // 填入名字
                    
                } else {
                    item.setExpertName("未知专家 (ID:" + expertId + ")");
                }
            });
        }

        return result;
    
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