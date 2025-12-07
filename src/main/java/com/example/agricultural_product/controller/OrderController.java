package com.example.agricultural_product.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.agricultural_product.pojo.Order;
import com.example.agricultural_product.pojo.OrderItem;
import com.example.agricultural_product.pojo.OrderItemReview;
import com.example.agricultural_product.dto.OrderDTO;
import com.example.agricultural_product.dto.CreateOrderRequest;
import com.example.agricultural_product.dto.OrderDetailResponse;
import com.example.agricultural_product.service.OrderService;
import com.example.agricultural_product.service.OrderItemReviewService;
import com.example.agricultural_product.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderItemReviewService orderItemReviewService;

    // ===== JWT 鉴权工具方法 =====
    private Long getUserIdFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("未提供有效的 JWT token");
        }
        String token = authHeader.substring(7);
        if (!JwtUtil.validateToken(token)) {
            throw new RuntimeException("无效的 JWT token");
        }
        return JwtUtil.getUserId(token);
    }

    /**
     * 创建订单
     */
    @PostMapping
    public ResponseEntity<Integer> createOrder(HttpServletRequest request,
                                               @RequestBody CreateOrderRequest req) {
        try {
            Long userId = getUserIdFromToken(request);

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
     * 获取当前用户订单列表
     */
    @GetMapping
    public ResponseEntity<List<OrderDTO>> getUserOrders(HttpServletRequest request) {
        // **注意：我们暂时去掉了分页参数，因为 Mapper 里的查询不是分页的**
        // @RequestParam(defaultValue = "1") Integer pageNum,
        // @RequestParam(defaultValue = "10") Integer pageSize
        
        Long userId = getUserIdFromToken(request);
        
        // **调用新的 Service 方法**
        List<OrderDTO> ordersWithItems = orderService.getMyOrdersWithItems(userId);
        
        return ResponseEntity.ok(ordersWithItems);
    }

    /**
     * 根据状态获取订单列表
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<Order>> getOrdersByStatus(HttpServletRequest request,
                                                         @PathVariable String status,
                                                         @RequestParam(defaultValue = "1") Integer pageNum,
                                                         @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = getUserIdFromToken(request);
        return ResponseEntity.ok(orderService.getOrdersByStatus(userId, status, pageNum, pageSize));
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDetailResponse> getOrderDetail(HttpServletRequest request,
                                                              @PathVariable Integer orderId) {
        Long userId = getUserIdFromToken(request);

        Order order = orderService.getOrderById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            return ResponseEntity.status(403).build(); // 防止越权访问
        }

        List<OrderItem> orderItems = orderService.getOrderItemsByOrderId(orderId);
        OrderDetailResponse response = new OrderDetailResponse();
        response.setOrder(order);
        response.setOrderItems(orderItems);
        return ResponseEntity.ok(response);
    }

    /**
     * 农户查看自己的销售订单
     */
    @GetMapping("/seller")
    public ResponseEntity<List<OrderDTO>> getSellerOrders(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        return ResponseEntity.ok(orderService.getSellerOrders(userId));
    }

    /**
     * 取消订单（用户）
     */
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<Boolean> cancelOrder(HttpServletRequest request,
                                               @PathVariable Integer orderId) {
        Long userId = getUserIdFromToken(request);
        boolean success = orderService.cancelOrder(orderId, userId);
        return ResponseEntity.ok(success);
    }

    /**
     * 确认收货（用户）
     */
    @PutMapping("/{orderId}/confirm")
    public ResponseEntity<Boolean> confirmOrder(HttpServletRequest request,
                                                @PathVariable Integer orderId) {
        Long userId = getUserIdFromToken(request);
        boolean success = orderService.confirmOrder(orderId, userId);
        return ResponseEntity.ok(success);
    }

    /**
     * 更新订单状态（管理员）
     */
    @PutMapping("/{orderId}/status")
    public ResponseEntity<Boolean> updateOrderStatus(HttpServletRequest request,
                                                     @PathVariable Integer orderId,
                                                     @RequestParam String status) {
        // 这里仍旧仅验证 token，有需要可后续加管理员角色判断
        getUserIdFromToken(request);
        boolean success = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(success);
    }

    /**
     * 卖家发货（针对单个订单项）
     */
    @PutMapping("/{orderId}/items/{itemId}/ship")
    public ResponseEntity<Boolean> shipOrderItem(HttpServletRequest request,
                                                 @PathVariable Integer orderId,
                                                 @PathVariable Integer itemId) {
        Long sellerId = getUserIdFromToken(request);
        boolean success = orderService.shipOrderItem(orderId, itemId, sellerId);
        return ResponseEntity.ok(success);
    }

    /**
     * 买家确认收货（针对单个订单项）
     */
    @PutMapping("/{orderId}/items/{itemId}/confirm")
    public ResponseEntity<Boolean> confirmOrderItem(HttpServletRequest request,
                                                    @PathVariable Integer orderId,
                                                    @PathVariable Integer itemId) {
        Long userId = getUserIdFromToken(request);
        boolean success = orderService.confirmOrderItem(orderId, itemId, userId);
        return ResponseEntity.ok(success);
    }

    /**
     * 买家申请退款（针对单个订单项）
     */
    @PutMapping("/{orderId}/items/{itemId}/refund/apply")
    public ResponseEntity<Boolean> applyRefundForItem(HttpServletRequest request,
                                                      @PathVariable Integer orderId,
                                                      @PathVariable Integer itemId,
                                                      @RequestParam(required = false) String reason) {
        Long userId = getUserIdFromToken(request);
        boolean success = orderService.applyRefundForItem(orderId, itemId, userId, reason);
        return ResponseEntity.ok(success);
    }

    /**
     * 卖家审核退款（针对单个订单项）
     */
    @PutMapping("/{orderId}/items/{itemId}/refund/review")
    public ResponseEntity<Boolean> reviewRefundForItem(HttpServletRequest request,
                                                       @PathVariable Integer orderId,
                                                       @PathVariable Integer itemId,
                                                       @RequestParam boolean approve,
                                                       @RequestParam(required = false) String rejectReason) {
        Long sellerId = getUserIdFromToken(request);
        boolean success = orderService.reviewRefundForItem(orderId, itemId, sellerId, approve, rejectReason);
        return ResponseEntity.ok(success);
    }

    /**
     * 针对某个订单项提交评价
     */
    @PostMapping("/{orderId}/items/{itemId}/review")
    public ResponseEntity<?> createOrderItemReview(HttpServletRequest request,
                                                   @PathVariable Integer orderId,
                                                   @PathVariable Integer itemId,
                                                   @RequestBody CreateReviewRequest body) {
        Long userId = getUserIdFromToken(request);

        // 如果 body 中没有 orderId/itemId，则从路径参数补全
        Integer reqOrderId = body.getOrderId() != null ? body.getOrderId() : orderId;
        Integer reqItemId = body.getItemId() != null ? body.getItemId() : itemId;

        OrderItemReview review = orderItemReviewService.createReview(
                userId,
                reqOrderId,
                reqItemId,
                body.getProductId(),
                body.getRating(),
                body.getContent(),
                body.getIsAnonymous()
        );
        return ResponseEntity.ok(review);
    }

    /**
     * 分页查询某个商品的评价列表
     * 为了兼容原有 /api/reviews/product/{productId}，这里提供统一入口
     */
    @GetMapping("/product/{productId}/reviews")
    public Page<OrderItemReview> listProductReviews(@PathVariable Integer productId,
                                                    @RequestParam(defaultValue = "1") Long page,
                                                    @RequestParam(defaultValue = "10") Long size) {
        return orderItemReviewService.listProductReviews(productId, page, size);
    }

    @Data
    public static class CreateReviewRequest {
        private Integer orderId;
        private Integer itemId;
        private Integer productId;
        private Integer rating; // 1-5
        private String content;
        private Boolean isAnonymous;
    }

    /**
     * 获取单个订单项的评价详情
     */
    @GetMapping("/items/{itemId}/review")
    public ResponseEntity<OrderItemReview> getReviewByItemId(HttpServletRequest request,
                                                             @PathVariable Integer itemId) {
        Long userId = getUserIdFromToken(request);
        
        OrderItemReview review = orderItemReviewService.getByItemId(itemId);
        
        if (review != null) {
            return ResponseEntity.ok(review);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
