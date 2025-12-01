package com.example.agricultural_product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.agricultural_product.pojo.OrderItem;

import java.util.List;

import org.apache.ibatis.annotations.*;

@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {

    /**
     * 辅助查询方法：根据订单ID查询其所有的订单项
     */
    @Select("SELECT " +
            "oi.item_id, oi.product_id, p.product_name, oi.quantity, oi.unit_price ," +
            "p.farmer_id " +
            "FROM tb_order_item oi " +
            "LEFT JOIN tb_product p ON oi.product_id = p.product_id " +
            "WHERE oi.order_id = #{orderId}")
    @Results({
        @Result(property = "itemId", column = "item_id", id = true),
        @Result(property = "productId", column = "product_id"),
        @Result(property = "productName", column = "product_name"),
        @Result(property = "quantity", column = "quantity"),
        @Result(property = "unitPrice", column = "unit_price"),
        @Result(property = "farmerId", column = "farmer_id")
    })
    List<OrderItem> findItemsByOrderId(Long orderId);
}
