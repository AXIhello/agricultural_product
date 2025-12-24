package com.example.agricultural_product.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateOrderRequest {
    private Integer addressId;
    private Long userCouponId;

    private BigDecimal originalAmount;
    private BigDecimal couponDiscount;
    private BigDecimal totalAmount;

    private List<OrderItemRequest> orderItems;
    
    @Data
    public static class OrderItemRequest {
        private Integer productId;
        private Integer quantity;
    }
}


