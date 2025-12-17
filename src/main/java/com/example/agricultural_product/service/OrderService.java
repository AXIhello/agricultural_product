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
     * @param userCouponId 使用的优惠券ID（可为空）
     * @return 订单ID，失败返回null
     */
    Integer createOrder(Long userId, Integer addressId, List<OrderItem> orderItems, Long userCouponId);
    
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

    /**
     * 卖家针对某个订单明细发货（仅该 item 状态变为 SHIPPED）。
     */
    boolean shipOrderItem(Integer orderId, Integer itemId, Long sellerId);

    /**
     * 买家确认某个订单明细收货（仅该 item 状态变为 RECEIVED）。
     */
    boolean confirmOrderItem(Integer orderId, Integer itemId, Long userId);

    /**
     * 买家针对某个订单明细申请退款（需要后续卖家审核）。
     */
    boolean applyRefundForItem(Integer orderId, Integer itemId, Long userId, String reason);

    /**
     * 卖家审核某个订单明细的退款申请（通过/拒绝）。
     */
    boolean reviewRefundForItem(Integer orderId, Integer itemId, Long sellerId, boolean approve, String rejectReason);

     /**
     * 获取农户（卖家）的销售订单列表
     */
    List<OrderDTO> getSellerOrders(Long farmerId);

    Integer getProductSalesLast30Days(Integer productId);
}
