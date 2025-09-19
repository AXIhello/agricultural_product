package com.example.agricultural_product.service.impl;

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
} 