package com.example.agricultural_product.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.agricultural_product.pojo.AgriculturalKnowledge;

public interface AgriculturalKnowledgeService {
    /**
     * 发布农业知识
     */
    AgriculturalKnowledge publishKnowledge(Long expertId, String title, String content);

    /**
     * 更新农业知识
     */
    boolean updateKnowledge(Long expertId, Integer knowledgeId, String title, String content);

    /**
     * 删除农业知识
     */
    boolean deleteKnowledge(Long expertId, Integer knowledgeId);

    /**
     * 获取所有农业知识
     */
    Page<AgriculturalKnowledge> getAllKnowledge(Integer pageNum, Integer pageSize);


    /**
     * 获取专家发布的知识列表
     */
    Page<AgriculturalKnowledge> getExpertKnowledgeList(Long expertId, Integer pageNum, Integer pageSize);

    /**
     * 获取知识详情
     */
    AgriculturalKnowledge getKnowledgeDetail(Integer knowledgeId);
}