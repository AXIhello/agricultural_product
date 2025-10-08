package com.example.agricultural_product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.agricultural_product.mapper.ProductMapper;
import com.example.agricultural_product.pojo.Product;
import com.example.agricultural_product.service.ProductService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

	@Override
	public Page<Product> getProductList(Integer pageNum, Integer pageSize) {
		Page<Product> page = new Page<>(pageNum, pageSize);
		LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(Product::getStatus, "active")
		       .orderByDesc(Product::getCreateTime);
		return page(page, wrapper);
	}

	@Override
	public Page<Product> searchProducts(String keyword, Integer pageNum, Integer pageSize) {
		Page<Product> page = new Page<>(pageNum, pageSize);
		LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(Product::getStatus, "active")
		       .like(Product::getProductName, keyword)
		       .orderByDesc(Product::getCreateTime);
		return page(page, wrapper);
	}

	@Override
	public Page<Product> getProductsByFarmerId(Long farmerId, Integer pageNum, Integer pageSize) {
		Page<Product> page = new Page<>(pageNum, pageSize);
		LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(Product::getFarmerId, farmerId)
		       .eq(Product::getStatus, "active")
		       .orderByDesc(Product::getCreateTime);
		return page(page, wrapper);
	}

	@Override
	public boolean publishProduct(Product product) {
		if (product == null) {
			return false;
		}
		// 默认发布为 active，设置创建和更新时间
		if (product.getStatus() == null) {
			product.setStatus("active");
		}
		LocalDateTime now = LocalDateTime.now();
		if (product.getCreateTime() == null) {
			product.setCreateTime(now);
		}
		product.setUpdateTime(now);
		return this.save(product);
	}

	@Override
	public boolean updateStock(Integer productId, Integer newStock) {
		if (productId == null || newStock == null || newStock < 0) {
			return false;
		}
		Product product = getById(productId);
		if (product == null) {
			return false;
		}
		product.setStock(newStock);
		product.setUpdateTime(LocalDateTime.now());
		return updateById(product);
	}
}