package com.example.agricultural_product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.agricultural_product.mapper.ExpertQuestionMapper;
import com.example.agricultural_product.mapper.UserMapper; 
import com.example.agricultural_product.pojo.ExpertQuestion;
import com.example.agricultural_product.pojo.User; 
import org.springframework.beans.factory.annotation.Autowired;
import com.example.agricultural_product.service.ExpertQuestionService;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ExpertQuestionServiceImpl extends ServiceImpl<ExpertQuestionMapper, ExpertQuestion> implements ExpertQuestionService {
	
	@Autowired
    private UserMapper userMapper;
	
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
		wrapper.orderByAsc(ExpertQuestion::getStatus)
		       .orderByDesc(ExpertQuestion::getCreateTime);
		
		long total = this.count(wrapper);  // 统计总数
		page.setTotal(total);
		
		// 手动分页
		List<ExpertQuestion> records = this.list(
			wrapper.last("LIMIT " + (pageNum - 1) * pageSize + "," + pageSize)
		);
		page.setRecords(records);

		fillFarmerNames(page.getRecords());
		
		return page;
	}

	@Override
	public Page<ExpertQuestion> search(String keyword, Integer pageNum, Integer pageSize) {
		Page<ExpertQuestion> page = new Page<>(pageNum, pageSize);
		LambdaQueryWrapper<ExpertQuestion> wrapper = new LambdaQueryWrapper<>();
		
		wrapper.and(w -> w.like(ExpertQuestion::getTitle, keyword)
		                  .or()
		                  .like(ExpertQuestion::getContent, keyword));

		wrapper.orderByAsc(ExpertQuestion::getStatus)
		       .orderByDesc(ExpertQuestion::getCreateTime);

		long total = this.count(wrapper);  // 使用 count() 方法
		page.setTotal(total);
		List<ExpertQuestion> records = this.list(wrapper.last("LIMIT " + (pageNum - 1) * pageSize + "," + pageSize));
		page.setRecords(records);

		fillFarmerNames(page.getRecords());

		return page;
	}

	 /**
     * 辅助方法：遍历问题列表，根据 farmerId 查用户表，填充 farmerName
     */
    private void fillFarmerNames(List<ExpertQuestion> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        for (ExpertQuestion question : records) {
            Long userId = question.getFarmerId();
            if (userId != null) {
                // 根据 ID 查询用户表
                User user = userMapper.selectById(userId);
                if (user != null) {
                    // 获取 name 字段 (注意：不是 userName，是你数据库里的 name 字段)
                    question.setFarmerName(user.getName());
                } else {
                    question.setFarmerName("未知用户");
                }
            }
        }
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