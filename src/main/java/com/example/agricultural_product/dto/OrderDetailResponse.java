package com.example.agricultural_product.dto;

import com.example.agricultural_product.pojo.Order;
import com.example.agricultural_product.pojo.OrderItem;
import lombok.Data;

import java.util.List;

@Data
public class OrderDetailResponse {
    private Order order;
    private List<OrderItem> orderItems;
}


