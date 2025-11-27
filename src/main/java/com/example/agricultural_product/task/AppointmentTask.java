package com.example.agricultural_product.task; // 注意包名

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.agricultural_product.mapper.ExpertConsultationMapper;
import com.example.agricultural_product.mapper.ExpertWorkingSlotMapper;
import com.example.agricultural_product.pojo.ExpertConsultation;
import com.example.agricultural_product.pojo.ExpertWorkingSlot;
import org.springframework.boot.context.event.ApplicationReadyEvent; 
import org.springframework.context.event.EventListener;   
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class AppointmentTask {

    private final ExpertWorkingSlotMapper slotMapper;
    private final ExpertConsultationMapper consultationMapper;

    public AppointmentTask(ExpertWorkingSlotMapper slotMapper, ExpertConsultationMapper consultationMapper) {
        this.slotMapper = slotMapper;
        this.consultationMapper = consultationMapper;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        System.out.println(" [系统启动] 正在扫描并处理过期预约数据...");
        closeExpiringSlots();
        completePastAppointments();
        System.out.println(" [系统启动] 过期数据处理完毕！");
    }


    /**
     * 任务1：每天凌晨 00:05 执行
     * 需求：在可预约的列表日期的前一天将状态转换为关闭
     * 逻辑：如果今天是 11月27日，那么 11月28日（明天）及之前的 Slot 都应该关闭（因为必须提前一天预约）
     */
    @Scheduled(cron = "0 5 0 * * ?") 
    @Transactional
    public void closeExpiringSlots() {
        // 获取明天的日期
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        UpdateWrapper<ExpertWorkingSlot> updateWrapper = new UpdateWrapper<>();
        // 逻辑：work_date <= 明天 AND status = 'open'
        // 意思是：如果是明天的号，今天已经是“前一天”了，或者更早日期的号，统统关闭
        updateWrapper.le("work_date", tomorrow) 
                     .eq("status", "open");
        
        ExpertWorkingSlot updateSlot = new ExpertWorkingSlot();
        updateSlot.setStatus("closed");
        
        int rows = slotMapper.update(updateSlot, updateWrapper);
        if (rows > 0) {
            System.out.println("🔥 [定时任务] 已自动关闭需提前预约的时间段: " + rows + " 个");
        }
    }

    /**
     * 任务2：每小时执行一次
     * 需求：对于预约好的咨询，一旦过了预约日期，无论是否取消状态都转换为已完成
     * 逻辑：只处理状态为 'scheduled' 且时间已过期的记录
     */
    @Scheduled(cron = "0 0 * * * ?") 
    @Transactional
    public void completePastAppointments() {
        LocalDateTime now = LocalDateTime.now();

        UpdateWrapper<ExpertConsultation> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lt("consultation_time", now) // 预约时间小于当前时间
                     .eq("status", "scheduled");   // 状态是“已预约”
        
        ExpertConsultation updateCons = new ExpertConsultation();
        updateCons.setStatus("completed");
        
        int rows = consultationMapper.update(updateCons, updateWrapper);
        if (rows > 0) {
            System.out.println("🔥 [定时任务] 已自动将过期预约标记为已完成: " + rows + " 个");
        }
    }
}