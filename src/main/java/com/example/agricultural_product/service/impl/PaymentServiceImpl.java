package com.example.agricultural_product.service.impl;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.example.agricultural_product.service.PaymentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Value("${alipay.app-id}")
    private String appId;

    @Value("${alipay.private-key}")
    private String privateKey;

    @Value("${alipay.alipay-public-key}")
    private String alipayPublicKey;

    @Value("${alipay.gateway-url}")
    private String gatewayUrl;

    @Value("${alipay.return-url}")
    private String returnUrl;

    @Value("${alipay.notify-url}")
    private String notifyUrl;

    private AlipayClient getAlipayClient() {
        return new DefaultAlipayClient(
            gatewayUrl,
            appId,
            privateKey,
            "json",
            "UTF-8",
            alipayPublicKey,
            "RSA2"
        );
    }

    @Override
    public String createAlipayUrl(String orderId, String orderName, BigDecimal amount) throws Exception {
        AlipayClient alipayClient = getAlipayClient();
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setReturnUrl(returnUrl);
        request.setNotifyUrl(notifyUrl);

        // 构建支付请求参数
        String bizContent = String.format(
            "{\"out_trade_no\":\"%s\"," +
            "\"total_amount\":\"%.2f\"," +
            "\"subject\":\"%s\"," +
            "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}",
            orderId,
            amount,
            orderName
        );
        request.setBizContent(bizContent);

        return alipayClient.pageExecute(request).getBody();
    }

    @Override
    public boolean handleAlipayCallback(Map<String, String> params) {
        try {
            // 1. 验证签名
            boolean signVerified = AlipaySignature.rsaCheckV1(
                params,
                alipayPublicKey,
                "UTF-8",
                "RSA2"
            );

            if (!signVerified) {
                return false;
            }

            // 2. 校验订单金额
            String tradeStatus = params.get("trade_status");
            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                String orderId = params.get("out_trade_no");
                String totalAmount = params.get("total_amount");
                // TODO: 验证订单金额是否一致
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}