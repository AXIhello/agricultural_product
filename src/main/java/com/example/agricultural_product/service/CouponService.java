package com.example.agricultural_product.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.agricultural_product.dto.coupon.*;
import com.example.agricultural_product.entity.CouponTemplate;
import com.example.agricultural_product.entity.UserCoupon;

import java.math.BigDecimal;
import java.util.List;

/**
 * 优惠券服务接口
 */
public interface CouponService {
    
    /**
     * 创建优惠券模板（管理员）
     */
    CouponTemplate createTemplate(CreateCouponTemplateDTO dto, Long operatorId);
    
    /**
     * 用户领取优惠券
     */
    UserCoupon receiveCoupon(Long userId, Integer templateId);
    
    /**
     * 批量发放优惠券（管理员）
     */
    void batchIssueCoupons(BatchIssueCouponDTO dto, Long operatorId);
    
    /**
     * 自动发放优惠券（新用户注册、完成订单等触发）
     */
    void autoIssueCoupons(Long userId, String activityType);
    
    /**
     * 查询用户优惠券列表
     * @param status 优惠券状态：unused/used/expired/all
     */
    Page<CouponDTO> getUserCoupons(Long userId, String status, Integer pageNum, Integer pageSize);
    
    /**
     * 查询订单可用优惠券
     * @param orderAmount 订单金额
     * @param productIds 商品ID列表（用于判断适用范围）
     */
    List<AvailableCouponDTO> getAvailableCoupons(Long userId, BigDecimal orderAmount, List<Integer> productIds);
    
    /**
     * 使用优惠券（下单时）
     * @return 实际优惠金额
     */
    BigDecimal useCoupon(Long userId, Long userCouponId, Integer orderId, BigDecimal orderAmount);
    
    /**
     * 锁定优惠券（下单时防止重复使用）
     */
    void lockCoupon(Long userCouponId);
    
    /**
     * 解锁优惠券（取消订单时）
     */
    void unlockCoupon(Long userCouponId);
    
    /**
     * 查询优惠券模板列表（管理员）
     */
    Page<CouponTemplate> getTemplates(String status, Integer pageNum, Integer pageSize);
    
    /**
     * 更新优惠券模板状态
     */
    void updateTemplateStatus(Integer templateId, String status);
    
    /**
     * 定时任务：过期优惠券处理
     */
    void expireCoupons();
}
