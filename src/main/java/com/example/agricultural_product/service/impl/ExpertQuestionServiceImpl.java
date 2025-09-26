package com.example.agricultural_product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.agricultural_product.mapper.ExpertQuestionMapper;
import com.example.agricultural_product.pojo.ExpertQuestion;
import com.example.agricultural_product.service.ExpertQuestionService;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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
		long total = this.count(wrapper);  // 使用 count() 方法
		page.setTotal(total);
		List<ExpertQuestion> records = this.list(wrapper.last("LIMIT " + (pageNum - 1) * pageSize + "," + pageSize));
		page.setRecords(records);
		return page;
	}

	@Override
	public Page<ExpertQuestion> listByFarmer(Long farmerId, Integer pageNum, Integer pageSize) {
		Page<ExpertQuestion> page = new Page<>(pageNum, pageSize);
		LambdaQueryWrapper<ExpertQuestion> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(ExpertQuestion::getFarmerId, farmerId).orderByDesc(ExpertQuestion::getCreateTime);
		return page(page, wrapper);
	}

	@Override
	public boolean acceptAnswer(Integer questionId, Integer answerId) {
		if (questionId == null || answerId == null) {
			return false;
		}
		ExpertQuestion q = getById(questionId);
		if (q == null) {
			return false;
		}
		q.setAcceptedAnswerId(answerId);
		q.setStatus("answered");
		q.setUpdateTime(LocalDateTime.now());
		return updateById(q);
	}
} 