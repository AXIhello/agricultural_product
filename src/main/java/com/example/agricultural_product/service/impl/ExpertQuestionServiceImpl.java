package com.example.agricultural_product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.agricultural_product.mapper.ExpertQuestionMapper;
import com.example.agricultural_product.pojo.ExpertQuestion;
import com.example.agricultural_product.service.ExpertQuestionService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ExpertQuestionServiceImpl extends ServiceImpl<ExpertQuestionMapper, ExpertQuestion> implements ExpertQuestionService {
	@Override
	public boolean publish(ExpertQuestion question) {
		if (question == null) {
			return false;
		}
		if (question.getStatus() == null) {
			question.setStatus("open");
		}
		LocalDateTime now = LocalDateTime.now();
		if (question.getCreateTime() == null) {
			question.setCreateTime(now);
		}
		question.setUpdateTime(now);
		return this.save(question);
	}

	@Override
	public Page<ExpertQuestion> listOpen(Integer pageNum, Integer pageSize) {
		Page<ExpertQuestion> page = new Page<>(pageNum, pageSize);
		LambdaQueryWrapper<ExpertQuestion> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(ExpertQuestion::getStatus, "open").orderByDesc(ExpertQuestion::getCreateTime);
		return page(page, wrapper);
	}

	@Override
	public Page<ExpertQuestion> search(String keyword, Integer pageNum, Integer pageSize) {
		Page<ExpertQuestion> page = new Page<>(pageNum, pageSize);
		LambdaQueryWrapper<ExpertQuestion> wrapper = new LambdaQueryWrapper<>();
		wrapper.like(ExpertQuestion::getTitle, keyword)
		       .or()
		       .like(ExpertQuestion::getContent, keyword)
		       .orderByDesc(ExpertQuestion::getCreateTime);
		return page(page, wrapper);
	}

	@Override
	public Page<ExpertQuestion> listByFarmer(Long farmerId, Integer pageNum, Integer pageSize) {
		Page<ExpertQuestion> page = new Page<>(pageNum, pageSize);
		LambdaQueryWrapper<ExpertQuestion> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(ExpertQuestion::getFarmerId, farmerId).orderByDesc(ExpertQuestion::getCreateTime);
		return page(page, wrapper);
	}

	@Override
	public boolean answerQuestion(Integer questionId, Long expertId, String answerContent) {
		if (questionId == null || expertId == null || answerContent == null || answerContent.trim().isEmpty()) {
			return false;
		}
		ExpertQuestion record = getById(questionId);
		if (record == null) {
			return false;
		}
		if (!"open".equalsIgnoreCase(record.getStatus())) {
			return false;
		}
		// 简化：此处默认专家ID有效；若需强校验，请在控制层或此处联表校验 tb_expert_profiles 是否存在
		record.setAnswerExpertId(expertId);
		record.setAnswerContent(answerContent);
		record.setAnswerTime(LocalDateTime.now());
		record.setStatus("answered");
		record.setUpdateTime(LocalDateTime.now());
		return updateById(record);
	}
} 