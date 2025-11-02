package com.example.agricultural_product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.agricultural_product.mapper.BankProductMapper;
import com.example.agricultural_product.pojo.BankProduct;
import com.example.agricultural_product.service.BankProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class BankProductServiceImpl extends ServiceImpl<BankProductMapper, BankProduct> implements BankProductService {

    @Autowired
    private BankProductMapper bankProductMapper;

    @Override
    public Integer createProduct(Long bankUserId, String name, String description,
                                 Integer termMonths, BigDecimal interestRate,
                                 BigDecimal minAmount, BigDecimal maxAmount) {
        BankProduct p = new BankProduct();
        p.setBankUserId(bankUserId);
        p.setProductName(name);
        p.setDescription(description);
        p.setTermMonths(termMonths);
        p.setInterestRate(interestRate);
        p.setMinAmount(minAmount);
        p.setMaxAmount(maxAmount);
        p.setStatus("active");
        p.setCreateTime(LocalDateTime.now());
        p.setUpdateTime(LocalDateTime.now());
        bankProductMapper.insert(p);
        return p.getProductId();
    }

    @Override
    public boolean updateStatus(Long bankUserId, Integer productId, String status) {
        BankProduct p = bankProductMapper.selectById(productId);
        if (p == null || !p.getBankUserId().equals(bankUserId)) return false;
        p.setStatus(status);
        p.setUpdateTime(LocalDateTime.now());
        return bankProductMapper.updateById(p) > 0;
    }

    @Override
    public Page<BankProduct> listMyProducts(Long bankUserId, Integer pageNum, Integer pageSize) {
        Page<BankProduct> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BankProduct> qw = new LambdaQueryWrapper<>();
        qw.eq(BankProduct::getBankUserId, bankUserId).orderByDesc(BankProduct::getCreateTime);
        return bankProductMapper.selectPage(page, qw);
    }

    @Override
    public Page<BankProduct> listAllProducts(Integer pageNum, Integer pageSize) {
        Page<BankProduct> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BankProduct> qw = new LambdaQueryWrapper<>();
        qw.eq(BankProduct::getStatus, "active")
          .orderByDesc(BankProduct::getCreateTime);
        return bankProductMapper.selectPage(page, qw);
    }
}