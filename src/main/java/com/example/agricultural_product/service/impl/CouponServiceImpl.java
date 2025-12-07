package com.example.agricultural_product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.agricultural_product.dto.coupon.*;
import com.example.agricultural_product.entity.*;
import com.example.agricultural_product.mapper.*;
import com.example.agricultural_product.service.CouponService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 优惠券服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {
    
    private final CouponTemplateMapper templateMapper;
    private final UserCouponMapper userCouponMapper;
    private final CouponIssueLogMapper issueLogMapper;
    private final CouponActivityMapper activityMapper;
    private final ObjectMapper objectMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CouponTemplate createTemplate(CreateCouponTemplateDTO dto, Long operatorId) {
        // 验证参数
        validateTemplateDTO(dto);
        
        CouponTemplate template = new CouponTemplate();
        BeanUtils.copyProperties(dto, template);
        template.setIssuedQuantity(0);
        template.setStatus("active");
        template.setIssuerId(operatorId);
        template.setIssuerType("admin");
        
        templateMapper.insert(template);
        log.info("创建优惠券模板成功，模板ID：{}, 操作人：{}", template.getTemplateId(), operatorId);
        return template;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserCoupon receiveCoupon(Long userId, Integer templateId) {
        // 1. 查询模板
        CouponTemplate template = templateMapper.selectById(templateId);
        if (template == null) {
            throw new RuntimeException("优惠券模板不存在");
        }
        
        // 2. 检查模板状态
        if (!"active".equals(template.getStatus())) {
            throw new RuntimeException("优惠券已下架或已过期");
        }
        
        // 3. 检查库存
        if (template.getTotalQuantity() != -1 && template.getIssuedQuantity() >= template.getTotalQuantity()) {
            throw new RuntimeException("优惠券已被领完");
        }
        
        // 4. 检查用户领取次数
        long userReceivedCount = userCouponMapper.selectCount(
            new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getTemplateId, templateId)
        );
        if (userReceivedCount >= template.getPerUserLimit()) {
            throw new RuntimeException("已达到领取上限");
        }
        
        // 5. 创建用户优惠券
        UserCoupon userCoupon = createUserCoupon(userId, template, "manual");
        userCouponMapper.insert(userCoupon);
        
        // 6. 更新已发放数量
        template.setIssuedQuantity(template.getIssuedQuantity() + 1);
        templateMapper.updateById(template);
        
        // 7. 记录发放日志
        recordIssueLog(templateId, userId, userCoupon.getUserCouponId(), "manual", "success", null, null);
        
        log.info("用户领取优惠券成功，用户ID：{}, 优惠券ID：{}", userId, userCoupon.getUserCouponId());
        return userCoupon;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchIssueCoupons(BatchIssueCouponDTO dto, Long operatorId) {
        CouponTemplate template = templateMapper.selectById(dto.getTemplateId());
        if (template == null) {
            throw new RuntimeException("优惠券模板不存在");
        }
        
        int successCount = 0;
        int failCount = 0;
        
        for (Long userId : dto.getUserIds()) {
            try {
                // 检查用户领取次数
                long userReceivedCount = userCouponMapper.selectCount(
                    new LambdaQueryWrapper<UserCoupon>()
                        .eq(UserCoupon::getUserId, userId)
                        .eq(UserCoupon::getTemplateId, dto.getTemplateId())
                );
                
                if (userReceivedCount >= template.getPerUserLimit()) {
                    recordIssueLog(dto.getTemplateId(), userId, null, "batch", "failed", "已达到领取上限", operatorId);
                    failCount++;
                    continue;
                }
                
                // 创建用户优惠券
                UserCoupon userCoupon = createUserCoupon(userId, template, "activity");
                userCouponMapper.insert(userCoupon);
                
                // 记录发放日志
                recordIssueLog(dto.getTemplateId(), userId, userCoupon.getUserCouponId(), "batch", "success", null, operatorId);
                successCount++;
                
            } catch (Exception e) {
                log.error("批量发放优惠券失败，用户ID：{}", userId, e);
                recordIssueLog(dto.getTemplateId(), userId, null, "batch", "failed", e.getMessage(), operatorId);
                failCount++;
            }
        }
        
        // 更新已发放数量
        template.setIssuedQuantity(template.getIssuedQuantity() + successCount);
        templateMapper.updateById(template);
        
        log.info("批量发放优惠券完成，成功：{}, 失败：{}", successCount, failCount);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void autoIssueCoupons(Long userId, String activityType) {
        // 查询符合条件的活动
        List<CouponActivity> activities = activityMapper.selectList(
            new LambdaQueryWrapper<CouponActivity>()
                .eq(CouponActivity::getActivityType, activityType)
                .eq(CouponActivity::getStatus, "active")
                .le(CouponActivity::getStartTime, LocalDateTime.now())
                .ge(CouponActivity::getEndTime, LocalDateTime.now())
                .orderByAsc(CouponActivity::getPriority)
        );
        
        if (activities.isEmpty()) {
            return;
        }
        
        // 执行活动发放
        for (CouponActivity activity : activities) {
            try {
                List<Integer> templateIds = objectMapper.readValue(
                    activity.getTemplateIds(), 
                    new TypeReference<List<Integer>>() {}
                );
                
                for (Integer templateId : templateIds) {
                    try {
                        CouponTemplate template = templateMapper.selectById(templateId);
                        if (template == null || !"active".equals(template.getStatus())) {
                            continue;
                        }
                        
                        // 检查用户领取次数
                        long userReceivedCount = userCouponMapper.selectCount(
                            new LambdaQueryWrapper<UserCoupon>()
                                .eq(UserCoupon::getUserId, userId)
                                .eq(UserCoupon::getTemplateId, templateId)
                        );
                        
                        if (userReceivedCount >= template.getPerUserLimit()) {
                            continue;
                        }
                        
                        // 创建用户优惠券
                        UserCoupon userCoupon = createUserCoupon(userId, template, "activity");
                        userCouponMapper.insert(userCoupon);
                        
                        // 更新已发放数量
                        template.setIssuedQuantity(template.getIssuedQuantity() + 1);
                        templateMapper.updateById(template);
                        
                        // 记录发放日志
                        recordIssueLog(templateId, userId, userCoupon.getUserCouponId(), "activity", "success", null, null);
                        
                        log.info("自动发放优惠券成功，用户ID：{}, 活动类型：{}, 优惠券ID：{}", 
                            userId, activityType, userCoupon.getUserCouponId());
                        
                    } catch (Exception e) {
                        log.error("自动发放优惠券失败，用户ID：{}, 模板ID：{}", userId, templateId, e);
                    }
                }
            } catch (Exception e) {
                log.error("解析活动模板ID列表失败，活动ID：{}", activity.getActivityId(), e);
            }
        }
    }
    
    @Override
    public Page<CouponDTO> getUserCoupons(Long userId, String status, Integer pageNum, Integer pageSize) {
        Page<UserCoupon> page = new Page<>(pageNum, pageSize);
        
        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<UserCoupon>()
            .eq(UserCoupon::getUserId, userId);
        
        if (!"all".equals(status)) {
            wrapper.eq(UserCoupon::getStatus, status);
        }
        
        wrapper.orderByDesc(UserCoupon::getCreateTime);
        
        Page<UserCoupon> userCouponPage = userCouponMapper.selectPage(page, wrapper);
        
        // 转换为DTO
        Page<CouponDTO> dtoPage = new Page<>(pageNum, pageSize);
        dtoPage.setTotal(userCouponPage.getTotal());
        dtoPage.setRecords(
            userCouponPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList())
        );
        
        return dtoPage;
    }
    
    @Override
    public List<AvailableCouponDTO> getAvailableCoupons(Long userId, BigDecimal orderAmount, List<Integer> productIds) {
        // 查询用户未使用的优惠券
        List<UserCoupon> userCoupons = userCouponMapper.selectList(
            new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getStatus, "unused")
                .le(UserCoupon::getValidStart, LocalDateTime.now())
                .ge(UserCoupon::getValidEnd, LocalDateTime.now())
                .orderByDesc(UserCoupon::getDiscountValue)
        );
        
        List<AvailableCouponDTO> result = new ArrayList<>();
        BigDecimal maxSaveAmount = BigDecimal.ZERO;
        
        for (UserCoupon userCoupon : userCoupons) {
            AvailableCouponDTO dto = new AvailableCouponDTO();
            BeanUtils.copyProperties(convertToDTO(userCoupon), dto);
            
            // 判断是否满足使用条件
            boolean meetsCondition = orderAmount.compareTo(userCoupon.getMinOrderAmount()) >= 0;
            dto.setMeetsCondition(meetsCondition);
            
            if (!meetsCondition) {
                dto.setUnavailableReason("订单金额未达到使用门槛");
                dto.setCanSaveAmount(BigDecimal.ZERO);
            } else {
                // 计算可节省金额
                BigDecimal saveAmount = calculateDiscountAmount(userCoupon, orderAmount);
                dto.setCanSaveAmount(saveAmount);
                
                if (saveAmount.compareTo(maxSaveAmount) > 0) {
                    maxSaveAmount = saveAmount;
                }
            }
            
            result.add(dto);
        }
        
        // 标记推荐优惠券
        final BigDecimal finalMaxSaveAmount = maxSaveAmount;
        result.forEach(dto -> {
            if (dto.getMeetsCondition() && dto.getCanSaveAmount().compareTo(finalMaxSaveAmount) == 0) {
                dto.setRecommended(true);
            } else {
                dto.setRecommended(false);
            }
        });
        
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BigDecimal useCoupon(Long userId, Long userCouponId, Integer orderId, BigDecimal orderAmount) {
        // 1. 查询优惠券
        UserCoupon userCoupon = userCouponMapper.selectById(userCouponId);
        if (userCoupon == null) {
            throw new RuntimeException("优惠券不存在");
        }
        
        // 2. 验证
        if (!userCoupon.getUserId().equals(userId)) {
            throw new RuntimeException("不能使用他人的优惠券");
        }
        
        if (!"locked".equals(userCoupon.getStatus())) {
            throw new RuntimeException("优惠券状态异常");
        }
        
        if (orderAmount.compareTo(userCoupon.getMinOrderAmount()) < 0) {
            throw new RuntimeException("订单金额未达到使用门槛");
        }
        
        // 3. 计算优惠金额
        BigDecimal discountAmount = calculateDiscountAmount(userCoupon, orderAmount);
        
        // 4. 更新优惠券状态
        userCoupon.setStatus("used");
        userCoupon.setUsedTime(LocalDateTime.now());
        userCoupon.setOrderId(orderId);
        userCoupon.setDiscountAmount(discountAmount);
        userCouponMapper.updateById(userCoupon);
        
        log.info("使用优惠券成功，优惠券ID：{}, 订单ID：{}, 优惠金额：{}", userCouponId, orderId, discountAmount);
        return discountAmount;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void lockCoupon(Long userCouponId) {
        UserCoupon userCoupon = userCouponMapper.selectById(userCouponId);
        if (userCoupon == null) {
            throw new RuntimeException("优惠券不存在");
        }
        
        if (!"unused".equals(userCoupon.getStatus())) {
            throw new RuntimeException("优惠券不可用");
        }
        
        userCoupon.setStatus("locked");
        userCouponMapper.updateById(userCoupon);
        log.info("锁定优惠券，优惠券ID：{}", userCouponId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unlockCoupon(Long userCouponId) {
        UserCoupon userCoupon = userCouponMapper.selectById(userCouponId);
        if (userCoupon != null && "locked".equals(userCoupon.getStatus())) {
            userCoupon.setStatus("unused");
            userCouponMapper.updateById(userCoupon);
            log.info("解锁优惠券，优惠券ID：{}", userCouponId);
        }
    }
    
    @Override
    public Page<CouponTemplate> getTemplates(String status, Integer pageNum, Integer pageSize) {
        Page<CouponTemplate> page = new Page<>(pageNum, pageSize);
        
        LambdaQueryWrapper<CouponTemplate> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !"all".equals(status)) {
            wrapper.eq(CouponTemplate::getStatus, status);
        }
        wrapper.orderByDesc(CouponTemplate::getCreateTime);
        
        return templateMapper.selectPage(page, wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTemplateStatus(Integer templateId, String status) {
        CouponTemplate template = templateMapper.selectById(templateId);
        if (template == null) {
            throw new RuntimeException("优惠券模板不存在");
        }
        
        template.setStatus(status);
        templateMapper.updateById(template);
        log.info("更新优惠券模板状态，模板ID：{}, 状态：{}", templateId, status);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void expireCoupons() {
        // 更新过期优惠券状态
        LambdaUpdateWrapper<UserCoupon> wrapper = new LambdaUpdateWrapper<UserCoupon>()
            .set(UserCoupon::getStatus, "expired")
            .eq(UserCoupon::getStatus, "unused")
            .lt(UserCoupon::getValidEnd, LocalDateTime.now());
        
        int count = userCouponMapper.update(null, wrapper);
        log.info("定时任务：处理过期优惠券，数量：{}", count);
    }
    
    // ==================== 私有辅助方法 ====================
    
    /**
     * 创建用户优惠券
     */
    private UserCoupon createUserCoupon(Long userId, CouponTemplate template, String acquireType) {
        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setUserId(userId);
        userCoupon.setTemplateId(template.getTemplateId());
        userCoupon.setCouponCode(generateCouponCode());
        userCoupon.setCouponName(template.getTemplateName());
        userCoupon.setCouponType(template.getCouponType());
        userCoupon.setDiscountValue(template.getDiscountValue());
        userCoupon.setMinOrderAmount(template.getMinOrderAmount());
        userCoupon.setStatus("unused");
        userCoupon.setReceiveTime(LocalDateTime.now());
        userCoupon.setAcquireType(acquireType);
        
        // 设置有效期
        if ("fixed".equals(template.getValidityType())) {
            userCoupon.setValidStart(template.getValidityStart());
            userCoupon.setValidEnd(template.getValidityEnd());
        } else {
            userCoupon.setValidStart(LocalDateTime.now());
            userCoupon.setValidEnd(LocalDateTime.now().plusDays(template.getValidityDays()));
        }
        
        return userCoupon;
    }
    
    /**
     * 生成优惠券券码
     */
    private String generateCouponCode() {
        return "CPN" + System.currentTimeMillis() + (int)(Math.random() * 10000);
    }
    
    /**
     * 记录发放日志
     */
    private void recordIssueLog(Integer templateId, Long userId, Long userCouponId, 
                                 String issueType, String issueStatus, String failReason, Long operatorId) {
        CouponIssueLog log = new CouponIssueLog();
        log.setTemplateId(templateId);
        log.setUserId(userId);
        log.setUserCouponId(userCouponId);
        log.setIssueType(issueType);
        log.setIssueStatus(issueStatus);
        log.setFailReason(failReason);
        log.setOperatorId(operatorId);
        issueLogMapper.insert(log);
    }
    
    /**
     * 计算优惠金额
     */
    private BigDecimal calculateDiscountAmount(UserCoupon coupon, BigDecimal orderAmount) {
        BigDecimal discountAmount = BigDecimal.ZERO;
        
        switch (coupon.getCouponType()) {
            case "full_reduction":
                // 满减券
                discountAmount = coupon.getDiscountValue();
                break;
                
            case "discount":
                // 折扣券
                discountAmount = orderAmount.multiply(BigDecimal.ONE.subtract(coupon.getDiscountValue()));
                // 如果有最大优惠限制
                CouponTemplate template = templateMapper.selectById(coupon.getTemplateId());
                if (template != null && template.getMaxDiscountAmount() != null) {
                    discountAmount = discountAmount.min(template.getMaxDiscountAmount());
                }
                break;
                
            case "fixed_amount":
                // 固定金额券
                discountAmount = coupon.getDiscountValue().min(orderAmount);
                break;
                
            case "free_shipping":
                // 免运费券
                discountAmount = coupon.getDiscountValue();
                break;
        }
        
        return discountAmount.setScale(2, BigDecimal.ROUND_HALF_UP);
    }
    
    /**
     * 转换为DTO
     */
    private CouponDTO convertToDTO(UserCoupon userCoupon) {
        CouponDTO dto = new CouponDTO();
        BeanUtils.copyProperties(userCoupon, dto);
        
        // 设置类型文本
        dto.setCouponTypeText(getCouponTypeText(userCoupon.getCouponType()));
        dto.setStatusText(getStatusText(userCoupon.getStatus()));
        
        // 判断是否即将过期（3天内）
        if ("unused".equals(userCoupon.getStatus())) {
            long daysUntilExpiry = java.time.Duration.between(
                LocalDateTime.now(), userCoupon.getValidEnd()
            ).toDays();
            dto.setExpiringSoon(daysUntilExpiry <= 3);
        } else {
            dto.setExpiringSoon(false);
        }
        
        // 判断是否可用
        dto.setAvailable("unused".equals(userCoupon.getStatus()) 
            && LocalDateTime.now().isAfter(userCoupon.getValidStart())
            && LocalDateTime.now().isBefore(userCoupon.getValidEnd()));
        
        // 从模板获取描述
        CouponTemplate template = templateMapper.selectById(userCoupon.getTemplateId());
        if (template != null) {
            dto.setDescription(template.getDescription());
        }
        
        return dto;
    }
    
    /**
     * 获取优惠券类型文本
     */
    private String getCouponTypeText(String couponType) {
        switch (couponType) {
            case "full_reduction": return "满减券";
            case "discount": return "折扣券";
            case "fixed_amount": return "直减券";
            case "free_shipping": return "免运费券";
            default: return "未知类型";
        }
    }
    
    /**
     * 获取状态文本
     */
    private String getStatusText(String status) {
        switch (status) {
            case "unused": return "未使用";
            case "used": return "已使用";
            case "expired": return "已过期";
            case "locked": return "已锁定";
            default: return "未知状态";
        }
    }
    
    /**
     * 验证模板DTO
     */
    private void validateTemplateDTO(CreateCouponTemplateDTO dto) {
        // 验证有效期
        if ("fixed".equals(dto.getValidityType())) {
            if (dto.getValidityStart() == null || dto.getValidityEnd() == null) {
                throw new RuntimeException("固定有效期需要设置开始和结束时间");
            }
            if (dto.getValidityEnd().isBefore(dto.getValidityStart())) {
                throw new RuntimeException("结束时间不能早于开始时间");
            }
        } else if ("relative".equals(dto.getValidityType())) {
            if (dto.getValidityDays() == null || dto.getValidityDays() <= 0) {
                throw new RuntimeException("相对有效期需要设置有效天数");
            }
        }
        
        // 验证优惠券类型
        if (!"full_reduction".equals(dto.getCouponType()) 
            && !"discount".equals(dto.getCouponType())
            && !"fixed_amount".equals(dto.getCouponType())
            && !"free_shipping".equals(dto.getCouponType())) {
            throw new RuntimeException("无效的优惠券类型");
        }
        
        // 验证折扣券
        if ("discount".equals(dto.getCouponType())) {
            if (dto.getDiscountValue().compareTo(BigDecimal.ZERO) <= 0 
                || dto.getDiscountValue().compareTo(BigDecimal.ONE) >= 0) {
                throw new RuntimeException("折扣率必须在0到1之间");
            }
        }
    }
}
