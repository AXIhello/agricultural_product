package com.example.agricultural_product.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.agricultural_product.pojo.Order;
import com.example.agricultural_product.pojo.OrderItem;
import com.example.agricultural_product.pojo.dto.CreateOrderRequest;
import com.example.agricultural_product.pojo.dto.OrderDetailResponse;
import com.example.agricultural_product.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 创建订单
     */
    @PostMapping
    public ResponseEntity<Integer> createOrder(@RequestBody CreateOrderRequest request, @RequestParam Long userId) {
        try {
            // 转换请求数据
            List<OrderItem> orderItems = request.getOrderItems().stream()
                    .map(item -> {
                        OrderItem orderItem = new OrderItem();
                        orderItem.setProductId(item.getProductId());
                        orderItem.setQuantity(item.getQuantity());
                        return orderItem;
                    })
                    .toList();
            
            Integer orderId = orderService.createOrder(userId, request.getAddressId(), orderItems);
            if (orderId != null) {
                return ResponseEntity.ok(orderId);
            } else {
                return ResponseEntity.badRequest().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 获取用户订单列表
     */
    @GetMapping
    public ResponseEntity<Page<Order>> getUserOrders(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return ResponseEntity.ok(orderService.getOrdersByUserId(userId, pageNum, pageSize));
    }

    /**
     * 根据状态获取订单列表
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<Order>> getOrdersByStatus(
            @PathVariable String status,
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return ResponseEntity.ok(orderService.getOrdersByStatus(userId, status, pageNum, pageSize));
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDetailResponse> getOrderDetail(@PathVariable Integer orderId) {
        Order order = orderService.getOrderById(orderId);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        
        List<OrderItem> orderItems = orderService.getOrderItemsByOrderId(orderId);
        
        OrderDetailResponse response = new OrderDetailResponse();
        response.setOrder(order);
        response.setOrderItems(orderItems);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 取消订单
     */
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<Boolean> cancelOrder(@PathVariable Integer orderId, @RequestParam Long userId) {
        boolean success = orderService.cancelOrder(orderId, userId);
        return ResponseEntity.ok(success);
    }

    /**
     * 确认收货
     */
    @PutMapping("/{orderId}/confirm")
    public ResponseEntity<Boolean> confirmOrder(@PathVariable Integer orderId, @RequestParam Long userId) {
        boolean success = orderService.confirmOrder(orderId, userId);
        return ResponseEntity.ok(success);
    }

    /**
     * 更新订单状态（管理员接口）
     */
    @PutMapping("/{orderId}/status")
    public ResponseEntity<Boolean> updateOrderStatus(
            @PathVariable Integer orderId,
            @RequestParam String status) {
        boolean success = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(success);
    }
}
