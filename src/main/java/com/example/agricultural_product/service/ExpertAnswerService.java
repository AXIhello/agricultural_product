package com.example.agricultural_product.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.agricultural_product.pojo.ExpertAnswer;
import com.example.agricultural_product.vo.AnswerVO;

public interface ExpertAnswerService extends IService<ExpertAnswer> {
	Page<AnswerVO> listByQuestion(Integer questionId, Integer pageNum, Integer pageSize);
} 