package com.example.agricultural_product.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.agricultural_product.pojo.Product;

public interface ProductService extends IService<Product> {
	// 分页获取商品列表
	Page<Product> getProductList(Integer pageNum, Integer pageSize);
	
	// 根据名称搜索商品
	Page<Product> searchProducts(String keyword, Integer pageNum, Integer pageSize);
	
	// 获取某个农户的商品列表
	Page<Product> getProductsByFarmerId(Long farmerId, Integer pageNum, Integer pageSize);

	// 发布商品
	boolean publishProduct(Product product);
}