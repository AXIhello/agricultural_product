package com.example.agricultural_product.service;

import com.example.agricultural_product.pojo.ExpertProfile;

import java.math.BigDecimal;

public interface ExpertProfileService {
    /**
     * 创建或更新专家档案
     */
    ExpertProfile saveOrUpdateProfile(Long expertId, ExpertProfile profile);

    /**
     * 获取专家档案
     */
    ExpertProfile getProfile(Long expertId);

    /**
     * 删除专家档案
     */
    boolean deleteProfile(Long expertId);

    /**
     * 更新专家评分
     */
    boolean updateRating(Long expertId, BigDecimal newRating);
}