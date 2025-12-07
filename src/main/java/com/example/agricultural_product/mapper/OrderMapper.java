// 文件路径: com/example/agricultural_product/mapper/OrderMapper.java

package com.example.agricultural_product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.agricultural_product.dto.OrderDTO;
import com.example.agricultural_product.pojo.Order;
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
                many = @Many(select = "com.example.agricultural_product.mapper.OrderItemMapper.findItemsByOrderId"))
    })
    List<OrderDTO> findOrdersWithItemsByUserId(Long userId);

    /**
     * 农户查询接口：查找包含该农户商品的订单
     * 逻辑：关联 tb_order -> tb_order_item -> tb_product，筛选 p.farmer_id
     * 使用 DISTINCT 去重（防止一个订单里买了该农户的多个商品导致订单重复显示）
     */
    @Select("SELECT DISTINCT o.* " +
            "FROM tb_order o " +
            "INNER JOIN tb_order_item oi ON o.order_id = oi.order_id " +
            "INNER JOIN tb_product p ON oi.product_id = p.product_id " +
            "WHERE p.farmer_id = #{farmerId} " +
            "ORDER BY o.create_time DESC")
    // 复用之前定义的 ResultMap，自动填充 orderItems
    @ResultMap("orderWithItemsResultMap") 
    List<OrderDTO> findOrdersBySellerId(Long farmerId);

}