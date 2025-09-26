package com.example.agricultural_product.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.agricultural_product.pojo.ExpertQuestion;

public interface ExpertQuestionService extends IService<ExpertQuestion> {
	boolean publish(ExpertQuestion question);

	Page<ExpertQuestion> listOpen(Integer pageNum, Integer pageSize);

	Page<ExpertQuestion> search(String keyword, Integer pageNum, Integer pageSize);

	Page<ExpertQuestion> listByFarmer(Long farmerId, Integer pageNum, Integer pageSize);

	// legacy single-answer method will be removed; use answer service instead

	boolean acceptAnswer(Integer questionId, Integer answerId);
} 