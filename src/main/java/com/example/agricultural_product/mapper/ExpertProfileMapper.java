package com.example.agricultural_product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.agricultural_product.dto.ExpertInfoDTO;
import com.example.agricultural_product.pojo.ExpertProfile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ExpertProfileMapper extends BaseMapper<ExpertProfile> {
    /**
     * 查询所有专家用户的整合信息。
     * 使用JOIN连接users表和tb_expert_profiles表。
     * @return 专家信息DTO列表
     */
    @Select("SELECT u.user_id as id, u.name as name, p.specialization as specialty, p.photo_url as avatar " +
            "FROM users u JOIN tb_expert_profiles p ON u.user_id = p.expert_id " +
            "WHERE u.role = 'expert'")
    List<ExpertInfoDTO> findAllExperts();
} 