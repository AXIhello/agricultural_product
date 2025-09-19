package com.example.agricultural_product.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.agricultural_product.pojo.PurchaseDemand;

public interface PurchaseDemandService extends IService<PurchaseDemand> {
	boolean publish(PurchaseDemand demand);

	// 分页获取开放的求购需求
	Page<PurchaseDemand> getDemandList(Integer pageNum, Integer pageSize);

	// 关键词搜索（按产品名、详情模糊匹配）
	Page<PurchaseDemand> searchDemands(String keyword, Integer pageNum, Integer pageSize);

	// 按买家ID获取其发布的求购需求
	Page<PurchaseDemand> getDemandsByBuyer(Long buyerId, Integer pageNum, Integer pageSize);
} 