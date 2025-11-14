package com.example.agricultural_product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.agricultural_product.mapper.ExpertAnswerMapper;
import com.example.agricultural_product.pojo.ExpertAnswer;
import com.example.agricultural_product.vo.AnswerVO;
import com.example.agricultural_product.service.ExpertAnswerService;
import org.springframework.stereotype.Service;

@Service
public class ExpertAnswerServiceImpl extends ServiceImpl<ExpertAnswerMapper, ExpertAnswer> implements ExpertAnswerService {
	@Override
	public Page<AnswerVO> listByQuestion(Integer questionId, Integer pageNum, Integer pageSize) {
		Page<AnswerVO> page = new Page<>(pageNum, pageSize);
		return baseMapper.findAnswersWithUserNameByQuestionId(page, questionId);
	}
} 