package com.example.agricultural_product.service.impl;

import com.example.agricultural_product.mapper.ExpertProfileMapper;
import com.example.agricultural_product.pojo.ExpertProfile;
import com.example.agricultural_product.service.ExpertProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class ExpertProfileServiceImpl implements ExpertProfileService {

    @Autowired
    private ExpertProfileMapper expertProfileMapper;

    @Override
    @Transactional
    public ExpertProfile saveOrUpdateProfile(Long expertId, ExpertProfile profile) {
        if (expertId == null) {
            throw new RuntimeException("专家ID不能为空");
        }

        // 检查是否已存在
        ExpertProfile existing = expertProfileMapper.selectById(expertId);
        
        if (existing == null) {
            // 新建档案
            profile.setExpertId(expertId);
            profile.setAverageRating(new BigDecimal("0.0"));
            profile.setCreateTime(LocalDateTime.now());
            profile.setUpdateTime(LocalDateTime.now());
            expertProfileMapper.insert(profile);
        } else {
            // 更新档案
            profile.setExpertId(expertId);
            profile.setUpdateTime(LocalDateTime.now());
            // 保留原有的评分和创建时间
            if (profile.getAverageRating() == null) {
                profile.setAverageRating(existing.getAverageRating());
            }
            profile.setCreateTime(existing.getCreateTime());
            expertProfileMapper.updateById(profile);
        }
        
        return expertProfileMapper.selectById(expertId);
    }

    @Override
    public ExpertProfile getProfile(Long expertId) {
        if (expertId == null) {
            return null;
        }
        return expertProfileMapper.selectById(expertId);
    }

    @Override
    @Transactional
    public boolean deleteProfile(Long expertId) {
        if (expertId == null) {
            return false;
        }
        return expertProfileMapper.deleteById(expertId) > 0;
    }

    @Override
    @Transactional
    public boolean updateRating(Long expertId, BigDecimal newRating) {
        if (expertId == null || newRating == null) {
            return false;
        }
        
        ExpertProfile profile = expertProfileMapper.selectById(expertId);
        if (profile == null) {
            return false;
        }
        
        profile.setAverageRating(newRating);
        profile.setUpdateTime(LocalDateTime.now());
        return expertProfileMapper.updateById(profile) > 0;
    }
}