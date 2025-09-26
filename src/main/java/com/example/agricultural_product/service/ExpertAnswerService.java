package com.example.agricultural_product.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.agricultural_product.pojo.ExpertAnswer;

public interface ExpertAnswerService extends IService<ExpertAnswer> {
	Page<ExpertAnswer> listByQuestion(Integer questionId, Integer pageNum, Integer pageSize);
} 