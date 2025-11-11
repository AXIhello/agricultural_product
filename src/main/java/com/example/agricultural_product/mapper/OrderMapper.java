// 文件路径: com/example/agricultural_product/mapper/OrderMapper.java

package com.example.agricultural_product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.agricultural_product.dto.OrderDTO;
import com.example.agricultural_product.pojo.Order;
import com.example.agricultural_product.pojo.OrderItem;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    /**
     * 主查询方法：根据用户ID获取订单列表及其关联的订单项
     */
    @Select("SELECT * FROM tb_order WHERE user_id = #{userId} ORDER BY create_time DESC")
    @Results(id = "orderWithItemsResultMap", value = {
        @Result(property = "orderId", column = "order_id", id = true),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "orderDate", column = "order_date"),
        @Result(property = "totalAmount", column = "total_amount"),
        @Result(property = "status", column = "status"),
        @Result(property = "orderItems", column = "order_id",
                many = @Many(select = "com.example.agricultural_product.mapper.OrderMapper.findItemsByOrderId"))
    })
    List<OrderDTO> findOrdersWithItemsByUserId(Long userId);

    /**
     * 辅助查询方法：根据订单ID查询其所有的订单项
     * 这里的 @Results 映射关系是专门为 OrderItem 类定制的
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