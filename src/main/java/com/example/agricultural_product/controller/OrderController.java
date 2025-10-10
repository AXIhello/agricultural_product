package com.example.agricultural_product.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.agricultural_product.pojo.Order;
import com.example.agricultural_product.pojo.OrderItem;
import com.example.agricultural_product.pojo.dto.CreateOrderRequest;
import com.example.agricultural_product.pojo.dto.OrderDetailResponse;
import com.example.agricultural_product.service.OrderService;
import com.example.agricultural_product.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    // ===== JWT 鉴权方法 =====
    private boolean checkToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return false;
        }
        String token = authHeader.substring(7);
        return JwtUtil.validateToken(token);
    }

    /**
     * 创建订单
     */
    @PostMapping
    public ResponseEntity<Integer> createOrder(HttpServletRequest request,
                                               @RequestBody CreateOrderRequest req,
                                               @RequestParam Long userId) {
        if (!checkToken(request)) return ResponseEntity.status(401).build();

        try {
            // 转换请求数据
            List<OrderItem> orderItems = req.getOrderItems().stream()
                    .map(item -> {
                        OrderItem orderItem = new OrderItem();
                        orderItem.setProductId(item.getProductId());
                        orderItem.setQuantity(item.getQuantity());
                        return orderItem;
                    }).toList();

            Integer orderId = orderService.createOrder(userId, req.getAddressId(), orderItems);
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
    public ResponseEntity<Page<Order>> getUserOrders(HttpServletRequest request,
                                                     @RequestParam Long userId,
                                                     @RequestParam(defaultValue = "1") Integer pageNum,
                                                     @RequestParam(defaultValue = "10") Integer pageSize) {
        if (!checkToken(request)) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(orderService.getOrdersByUserId(userId, pageNum, pageSize));
    }

    /**
     * 根据状态获取订单列表
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<Order>> getOrdersByStatus(HttpServletRequest request,
                                                         @PathVariable String status,
                                                         @RequestParam Long userId,
                                                         @RequestParam(defaultValue = "1") Integer pageNum,
                                                         @RequestParam(defaultValue = "10") Integer pageSize) {
        if (!checkToken(request)) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(orderService.getOrdersByStatus(userId, status, pageNum, pageSize));
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDetailResponse> getOrderDetail(HttpServletRequest request,
                                                              @PathVariable Integer orderId) {
        if (!checkToken(request)) return ResponseEntity.status(401).build();

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
    public ResponseEntity<Boolean> cancelOrder(HttpServletRequest request,
                                               @PathVariable Integer orderId,
                                               @RequestParam Long userId) {
        if (!checkToken(request)) return ResponseEntity.status(401).build();
        boolean success = orderService.cancelOrder(orderId, userId);
        return ResponseEntity.ok(success);
    }

    /**
     * 确认收货
     */
    @PutMapping("/{orderId}/confirm")
    public ResponseEntity<Boolean> confirmOrder(HttpServletRequest request,
                                                @PathVariable Integer orderId,
                                                @RequestParam Long userId) {
        if (!checkToken(request)) return ResponseEntity.status(401).build();
        boolean success = orderService.confirmOrder(orderId, userId);
        return ResponseEntity.ok(success);
    }

    /**
     * 更新订单状态（管理员接口）
     */
    @PutMapping("/{orderId}/status")
    public ResponseEntity<Boolean> updateOrderStatus(HttpServletRequest request,
                                                     @PathVariable Integer orderId,
                                                     @RequestParam String status) {
        if (!checkToken(request)) return ResponseEntity.status(401).build();
        boolean success = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(success);
    }
}
