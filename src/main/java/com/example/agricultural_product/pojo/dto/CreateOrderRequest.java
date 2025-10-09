package com.example.agricultural_product.pojo.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {
    private Integer addressId;
    private List<OrderItemRequest> orderItems;
    
    @Data
    public static class OrderItemRequest {
        private Integer productId;
        private Integer quantity;
    }
}
