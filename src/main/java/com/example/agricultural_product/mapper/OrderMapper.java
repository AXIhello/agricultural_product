package com.example.agricultural_product.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page; 
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.agricultural_product.pojo.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.One;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    @Select("SELECT * FROM tb_order WHERE user_id = #{userId} ORDER BY create_time DESC")
    @Results({
       
        @Result(property = "address", column = "shipping_address_id", javaType = com.example.agricultural_product.pojo.Address.class,
            one = @One(select = "com.example.agricultural_product.mapper.AddressMapper.selectById"))
    })

    Page<Order> selectOrderPageWithAddress(Page<Order> page, @Param("userId") Long userId);
}
