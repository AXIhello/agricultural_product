package com.example.agricultural_product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.agricultural_product.pojo.PurchaseDemand;

public interface PurchaseDemandService extends IService<PurchaseDemand> {
	boolean publish(PurchaseDemand demand);
} 