package com.example.agricultural_product.controller;

import com.example.agricultural_product.service.PaymentService;
import com.example.agricultural_product.service.OrderService;
import com.example.agricultural_product.pojo.Order;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/alipay")
public class AlipayController {
    private static final Logger logger = LoggerFactory.getLogger(AlipayController.class);

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private OrderService orderService;

    // 状态映射：外部 <-> 内部
    private String externalToInternal(String external) {
        if (external == null) return null;
        switch (external) {
            case "paid": return "completed";
            case "unpaid": return "pending";
            default: return external;
        }
    }

    private String internalToExternal(String internal) {
        if (internal == null) return null;
        switch (internal) {
            case "completed": return "paid";
            case "pending": return "unpaid";
            default: return internal;
        }
    }

    /**
     * 创建支付
     */
    @PostMapping("/pay/{orderId}")
    public String pay(@PathVariable Integer orderId) throws Exception {
        Order order = orderService.getOrderById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        if (!"pending".equals(order.getStatus())) {
            throw new RuntimeException("订单状态不正确，只有 pending 状态可支付");
        }

        String orderName = "农产品订单-" + orderId;
        return paymentService.createAlipayUrl(
                orderId.toString(),
                orderName,
                order.getTotalAmount()
        );
    }

    /**
     * 支付宝服务器异步通知
     */
    @PostMapping("/notify")
    public String notify(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((k, v) -> params.put(k, String.join(",", v)));
        logger.info("Alipay notify received: {}", params);

        try {
            boolean signVerified = paymentService.handleAlipayCallback(params);
            logger.info("Alipay signature/processing result: {}", signVerified);

            if (!signVerified) {
                logger.warn("Alipay notify signature verification failed.");
                return "fail";
            }

            String outTradeNo = params.get("out_trade_no");
            String tradeStatus = params.get("trade_status");
            String totalAmountStr = params.get("total_amount");

            if (outTradeNo == null || totalAmountStr == null) {
                logger.error("Alipay notify missing required parameters");
                return "fail";
            }

            if (!"TRADE_SUCCESS".equals(tradeStatus) && !"TRADE_FINISHED".equals(tradeStatus)) {
                logger.info("Alipay trade_status is not success: {}", tradeStatus);
                return "fail";
            }

            Integer orderId;
            try {
                orderId = Integer.parseInt(outTradeNo);
            } catch (NumberFormatException nfe) {
                logger.error("Invalid out_trade_no format: {}", outTradeNo, nfe);
                return "fail";
            }

            Order order = orderService.getOrderById(orderId);
            if (order == null) {
                logger.error("Order not found for id: {}", orderId);
                return "fail";
            }

            BigDecimal notifyAmount = new BigDecimal(totalAmountStr);
            if (order.getTotalAmount() == null || notifyAmount.compareTo(order.getTotalAmount()) != 0) {
                logger.error("Amount mismatch: notify={}, order={}", notifyAmount, order.getTotalAmount());
                return "fail";
            }

            if (!"completed".equals(order.getStatus())) {
                boolean updated = orderService.updateOrderStatus(orderId, "completed");
                logger.info("Update order status result for {}: {}", orderId, updated);
            } else {
                logger.info("Order {} already completed", orderId);
            }

            return "success";
        } catch (Exception e) {
            logger.error("Exception handling Alipay notify", e);
            return "fail";
        }
    }

    /**
     * 支付成功页面跳转（同步通知）
     */
    @GetMapping("/return")
    public String returnPage(HttpServletRequest request) {
        try {
            String outTradeNo = request.getParameter("out_trade_no");
            String totalAmount = request.getParameter("total_amount");

            if (outTradeNo == null) {
                return "支付失败：缺少订单号";
            }

            Integer orderId = Integer.parseInt(outTradeNo);
            Order order = orderService.getOrderById(orderId);
            if (order == null) {
                return "支付失败：订单不存在";
            }

            if (!"completed".equals(order.getStatus())) {
                boolean updated = orderService.updateOrderStatus(orderId, "completed");
                logger.info("订单 {} 状态更新为 completed，结果={}", orderId, updated);
            } else {
                logger.info("订单 {} 已是 completed 状态，无需重复更新", orderId);
            }

            // 返回支付成功提示（可换成前端页面跳转）
            return "支付成功！订单号：" + outTradeNo + "，金额：" + totalAmount;
        } catch (Exception e) {
            logger.error("处理支付回调异常", e);
            return "支付异常：" + e.getMessage();
        }
    }
}
