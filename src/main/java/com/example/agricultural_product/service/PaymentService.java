package com.example.agricultural_product.service;
import java.math.BigDecimal;
import java.util.Map;
public interface PaymentService {
    /**
     * 创建支付宝支付链接
     */
    String createAlipayUrl(String orderId, String orderName, BigDecimal amount) throws Exception;

    /**
     * 处理支付宝支付结果回调
     */
    boolean handleAlipayCallback(Map<String, String> params);
}