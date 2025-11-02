package com.example.agricultural_product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.agricultural_product.mapper.AgriculturalKnowledgeMapper;
import com.example.agricultural_product.pojo.AgriculturalKnowledge;
import com.example.agricultural_product.service.AgriculturalKnowledgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AgriculturalKnowledgeServiceImpl implements AgriculturalKnowledgeService {

    @Autowired
    private AgriculturalKnowledgeMapper knowledgeMapper;

    @Override
    @Transactional
    public AgriculturalKnowledge publishKnowledge(Long expertId, String title, String content) {
        if (expertId == null || title == null || content == null) {
            throw new RuntimeException("参数不完整");
        }

        AgriculturalKnowledge knowledge = new AgriculturalKnowledge();
        knowledge.setExpertId(expertId);
        knowledge.setTitle(title);
        knowledge.setContent(content);
        knowledge.setCreateTime(LocalDateTime.now());
        knowledge.setUpdateTime(LocalDateTime.now());

        knowledgeMapper.insert(knowledge);
        return knowledge;
    }

    @Override
    @Transactional
    public boolean updateKnowledge(Long expertId, Integer knowledgeId, String title, String content) {
        LambdaQueryWrapper<AgriculturalKnowledge> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AgriculturalKnowledge::getKnowledgeId, knowledgeId)
               .eq(AgriculturalKnowledge::getExpertId, expertId);

        AgriculturalKnowledge knowledge = knowledgeMapper.selectOne(wrapper);
        if (knowledge == null) {
            return false;
        }

        knowledge.setTitle(title);
        knowledge.setContent(content);
        knowledge.setUpdateTime(LocalDateTime.now());

        return knowledgeMapper.updateById(knowledge) > 0;
    }

    @Override
    @Transactional
    public boolean deleteKnowledge(Long expertId, Integer knowledgeId) {
        LambdaQueryWrapper<AgriculturalKnowledge> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AgriculturalKnowledge::getKnowledgeId, knowledgeId)
               .eq(AgriculturalKnowledge::getExpertId, expertId);

        return knowledgeMapper.delete(wrapper) > 0;
    }

    @Override
    public Page<AgriculturalKnowledge> getExpertKnowledgeList(Long expertId, Integer pageNum, Integer pageSize) {
        Page<AgriculturalKnowledge> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AgriculturalKnowledge> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AgriculturalKnowledge::getExpertId, expertId)
               .orderByDesc(AgriculturalKnowledge::getCreateTime);

        return knowledgeMapper.selectPage(page, wrapper);
    }

    @Override
    public AgriculturalKnowledge getKnowledgeDetail(Integer knowledgeId) {
        return knowledgeMapper.selectById(knowledgeId);
    }
}