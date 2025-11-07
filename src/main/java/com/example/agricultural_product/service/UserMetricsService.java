package com.example.agricultural_product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.agricultural_product.mapper.FinancingMapper;
import com.example.agricultural_product.mapper.UserMapper;
import com.example.agricultural_product.pojo.Financing;
import com.example.agricultural_product.pojo.User;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserMetricsService {
    private static final Logger logger = LoggerFactory.getLogger(UserMetricsService.class);

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FinancingMapper financingMapper;

    /**
     * 服务启动时更新所有用户指标
     */
    @PostConstruct
    public void initializeMetrics() {
        logger.info("Starting initial metrics update for all users...");
        try {
            updateAllUsersMetrics();
            logger.info("Initial metrics update completed successfully");
        } catch (Exception e) {
            logger.error("Error during initial metrics update", e);
        }
    }

    /**
     * 更新所有用户的融资指标
     */
    @Transactional
    public void updateAllUsersMetrics() {
        logger.info("Updating metrics for all users...");

        // 获取所有农户用户
        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<>();
        query.eq(User::getRole, "farmer");
        List<User> farmers = userMapper.selectList(query);

        int totalUsers = farmers.size();
        int updatedCount = 0;

        // 更新每个农户的指标
        for (User farmer : farmers) {
            try {
                updateUserFinancingMetrics(farmer.getUserId());
                updatedCount++;

                // 每更新10个用户记录一次日志
                if (updatedCount % 10 == 0) {
                    logger.info("Updated metrics for {}/{} users", updatedCount, totalUsers);
                }
            } catch (Exception e) {
                logger.error("Error updating metrics for user {}", farmer.getUserId(), e);
            }
        }

        logger.info("Completed metrics update for all users. Total updated: {}/{}", updatedCount, totalUsers);
    }

    /**
     * 计算并更新用户的融资指标
     */
    @Transactional
    public void updateUserFinancingMetrics(Long userId) {
        // 1. 获取该用户的所有融资记录
        LambdaQueryWrapper<Financing> query = new LambdaQueryWrapper<>();
        query.eq(Financing::getInitiatingFarmerId, userId);
        List<Financing> financings = financingMapper.selectList(query);

        if (financings.isEmpty()) {
            // 如果没有融资记录，设置默认值
            User user = new User();
            user.setUserId(userId);
            user.setHistoricalSuccessRate(BigDecimal.ZERO);
            user.setAverageFinancingAmount(BigDecimal.ZERO);
            user.setFinancingActivityLevel("low");
            userMapper.updateById(user);
            return;
        }

        // 2. 计算成功率
        long totalCount = financings.size();
        long successCount = financings.stream()
                .filter(f -> "approved".equals(f.getApplicationStatus()))
                .count();

        BigDecimal successRate = BigDecimal.valueOf(successCount)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalCount), 2, RoundingMode.HALF_UP);

        // 3. 计算平均融资金额
        BigDecimal totalAmount = financings.stream()
                .map(Financing::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageAmount = totalAmount.divide(BigDecimal.valueOf(totalCount), 2, RoundingMode.HALF_UP);

        // 4. 计算融资活跃度
        String activityLevel = calculateActivityLevel(financings);

        // 5. 更新用户表
        User user = new User();
        user.setUserId(userId);
        user.setHistoricalSuccessRate(successRate);
        user.setAverageFinancingAmount(averageAmount);
        user.setFinancingActivityLevel(activityLevel);
        userMapper.updateById(user);
    }

    /**
     * 计算融资活跃度
     */
    private String calculateActivityLevel(List<Financing> financings) {
        // 获取最近3个月的融资次数
        LocalDateTime threeMonthsAgo = LocalDateTime.now().minusMonths(3);

        long recentCount = financings.stream()
                .filter(f -> f.getCreateTime().isAfter(threeMonthsAgo))
                .count();

        // 定义活跃度标准
        if (recentCount >= 5) {
            return "high";
        } else if (recentCount >= 2) {
            return "medium";
        } else {
            return "low";
        }
    }
}