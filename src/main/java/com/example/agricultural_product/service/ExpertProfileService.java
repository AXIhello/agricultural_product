package com.example.agricultural_product.service;

import com.example.agricultural_product.pojo.ExpertProfile;
import com.example.agricultural_product.dto.ExpertInfoDTO;

import java.math.BigDecimal;
import java.util.List;

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

    /**
     * 获取所有专家的信息列表，展示于首页
     * @return 包含所有专家信息的DTO列表
     */
    List<ExpertInfoDTO> getAllExperts();

    /**
     * 更新专家照片URL
     * @param expertId 专家ID
     * @param newPhotoUrl 新的照片URL路径
     * @return 是否更新成功
     */
    boolean updatePhotoUrl(Long expertId, String newPhotoUrl);
}