package com.example.agricultural_product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.agricultural_product.entity.CouponIssueLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券发放记录Mapper
 */
@Mapper
public interface CouponIssueLogMapper extends BaseMapper<CouponIssueLog> {
}
