package com.example.agricultural_product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.agricultural_product.mapper.PurchaseDemandMapper;
import com.example.agricultural_product.pojo.PurchaseDemand;
import com.example.agricultural_product.service.PurchaseDemandService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PurchaseDemandServiceImpl extends ServiceImpl<PurchaseDemandMapper, PurchaseDemand> implements PurchaseDemandService {
	@Override
	public boolean publish(PurchaseDemand demand) {
		if (demand == null) {
			return false;
		}
		if (demand.getStatus() == null) {
			demand.setStatus("open");
		}
		LocalDateTime now = LocalDateTime.now();
		if (demand.getCreateTime() == null) {
			demand.setCreateTime(now);
		}
		demand.setUpdateTime(now);
		return this.save(demand);
	}

	@Override
	public Page<PurchaseDemand> getDemandList(Integer pageNum, Integer pageSize) {
		Page<PurchaseDemand> page = new Page<>(pageNum, pageSize);
		LambdaQueryWrapper<PurchaseDemand> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(PurchaseDemand::getStatus, "open")
		       .orderByDesc(PurchaseDemand::getCreateTime);
		return page(page, wrapper);
	}

	@Override
	public Page<PurchaseDemand> searchDemands(String keyword, Integer pageNum, Integer pageSize) {
		Page<PurchaseDemand> page = new Page<>(pageNum, pageSize);
		LambdaQueryWrapper<PurchaseDemand> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(PurchaseDemand::getStatus, "open")
		       .and(w -> w.like(PurchaseDemand::getProductNameDesired, keyword)
		                .or()
		                .like(PurchaseDemand::getDetails, keyword))
		       .orderByDesc(PurchaseDemand::getCreateTime);
		return page(page, wrapper);
	}

	@Override
	public Page<PurchaseDemand> getDemandsByBuyer(Long buyerId, Integer pageNum, Integer pageSize) {
		Page<PurchaseDemand> page = new Page<>(pageNum, pageSize);
		LambdaQueryWrapper<PurchaseDemand> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(PurchaseDemand::getBuyerId, buyerId)
		       .orderByDesc(PurchaseDemand::getCreateTime);
		return page(page, wrapper);
	}
} 