package com.example.agricultural_product.schedule;

import com.example.agricultural_product.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 优惠券定时任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CouponScheduleTask {
    
    private final CouponService couponService;
    
    /**
     * 每天凌晨1点执行，处理过期优惠券
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void expireCoupons() {
        log.info("开始执行优惠券过期处理定时任务");
        try {
            couponService.expireCoupons();
            log.info("优惠券过期处理定时任务执行完成");
        } catch (Exception e) {
            log.error("优惠券过期处理定时任务执行失败", e);
        }
    }
}
