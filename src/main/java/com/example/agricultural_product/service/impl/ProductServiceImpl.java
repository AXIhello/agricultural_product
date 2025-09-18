package com.example.agricultural_product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.agricultural_product.mapper.ProductMapper;
import com.example.agricultural_product.pojo.Product;
import com.example.agricultural_product.service.ProductService;
import org.springframework.stereotype.Service;

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
}