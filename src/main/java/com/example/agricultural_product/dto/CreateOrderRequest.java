package com.example.agricultural_product.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {
    private Integer addressId;
    private List<OrderItemRequest> orderItems;
    private Long userCouponId; // 使用的优惠券ID（可选）
    
    @Data
    public static class OrderItemRequest {
        private Integer productId;
        private Integer quantity;
    }
}


