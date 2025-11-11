package com.example.agricultural_product.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.agricultural_product.dto.OrderDTO;
import com.example.agricultural_product.pojo.Order;
import com.example.agricultural_product.pojo.OrderItem;

import java.util.List;

public interface OrderService extends IService<Order> {
    
    /**
     * 创建订单
     * @param userId 用户ID
     * @param addressId 收货地址ID
     * @param orderItems 订单项列表
     * @return 订单ID，失败返回null
     */
    Integer createOrder(Long userId, Integer addressId, List<OrderItem> orderItems);
    
    // /**
    //  * 根据用户ID分页查询订单
    //  * @param userId 用户ID
    //  * @param pageNum 页码
    //  * @param pageSize 页大小
    //  * @return 订单分页数据
    //  */
    // Page<Order> getOrdersByUserId(Long userId, Integer pageNum, Integer pageSize);
    
    /**
     * 根据订单ID获取订单详情
     * @param orderId 订单ID
     * @return 订单对象
     */
    Order getOrderById(Integer orderId);
    
    /**
     * 根据订单ID获取订单项列表
     * @param orderId 订单ID
     * @return 订单项列表
     */
    List<OrderItem> getOrderItemsByOrderId(Integer orderId);
    
    /**
     * 更新订单状态
     * @param orderId 订单ID
     * @param status 新状态
     * @return 是否成功
     */
    boolean updateOrderStatus(Integer orderId, String status);
    
    /**
     * 取消订单
     * @param orderId 订单ID
     * @param userId 用户ID（验证权限）
     * @return 是否成功
     */
    boolean cancelOrder(Integer orderId, Long userId);
    
    /**
     * 确认收货
     * @param orderId 订单ID
     * @param userId 用户ID（验证权限）
     * @return 是否成功
     */
    boolean confirmOrder(Integer orderId, Long userId);
    
    /**
     * 根据状态查询订单
     * @param userId 用户ID
     * @param status 订单状态
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 订单分页数据
     */
    Page<Order> getOrdersByStatus(Long userId, String status, Integer pageNum, Integer pageSize);

    List<OrderDTO> getMyOrdersWithItems(Long userId);
}


