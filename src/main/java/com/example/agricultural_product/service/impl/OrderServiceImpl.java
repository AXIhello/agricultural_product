package com.example.agricultural_product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.agricultural_product.dto.OrderDTO;
import com.example.agricultural_product.mapper.OrderItemMapper;
import com.example.agricultural_product.mapper.OrderMapper;
import com.example.agricultural_product.mapper.ProductMapper;
import com.example.agricultural_product.pojo.Order;
import com.example.agricultural_product.pojo.OrderItem;
import com.example.agricultural_product.pojo.Product;
import com.example.agricultural_product.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private String buildStockKey(Integer productId) {
        return "seckill:stock:" + productId;
    }

    @Override
    @Transactional
    public Integer createOrder(Long userId, Integer addressId, List<OrderItem> orderItems) {
        if (userId == null || addressId == null || orderItems == null || orderItems.isEmpty()) {
            return null;
        }
        // 计算总金额并验证商品（使用 Redis 预减库存防超卖）
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItem item : orderItems) {
            Integer productId = item.getProductId();
            String stockKey = buildStockKey(productId);
            // 如果 Redis 中没有该商品库存，则初始化一次（从数据库加载）
            if (Boolean.FALSE.equals(stringRedisTemplate.hasKey(stockKey))) {
                Product dbProduct = productMapper.selectById(productId);
                if (dbProduct == null || !"active".equals(dbProduct.getStatus())) {
                    throw new RuntimeException("商品不存在或已下架");
                }
                // 将当前数据库库存写入 Redis
                stringRedisTemplate.opsForValue().set(stockKey, String.valueOf(dbProduct.getStock()));
            }
            // 使用 Redis 进行预减库存（高并发下避免超卖）
            Long remain = stringRedisTemplate.opsForValue().decrement(stockKey, item.getQuantity());
            if (remain == null || remain < 0) {
                // 回滚本次预减（加回去）
                stringRedisTemplate.opsForValue().increment(stockKey, item.getQuantity());
                throw new RuntimeException("商品库存不足");
            }

            Product product = productMapper.selectById(productId);
            if (product == null || !"active".equals(product.getStatus())) {
                throw new RuntimeException("商品不存在或已下架");
            }
            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
            item.setUnitPrice(product.getPrice());
        }
        Order order = new Order();
        order.setUserId(userId);
        order.setOrderDate(LocalDateTime.now());
        order.setTotalAmount(totalAmount);
        order.setStatus("pending");
        order.setShippingAddressId(addressId);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        if (this.save(order)) {
            for (OrderItem item : orderItems) {
                item.setOrderId(order.getOrderId());
                item.setCreateTime(LocalDateTime.now());
                orderItemMapper.insert(item);
                // 这里不再扣减库存，改到支付环节
            }
            return order.getOrderId();
        }
        return null;
    }

    @Override
    public List<OrderDTO> getMyOrdersWithItems(Long userId) {
        // 直接调用 Mapper 中的方法
        return this.baseMapper.findOrdersWithItemsByUserId(userId);
    }

    @Override
    public Order getOrderById(Integer orderId) {
        return this.getById(orderId);
    }

    @Override
    public List<OrderItem> getOrderItemsByOrderId(Integer orderId) {
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getOrderId, orderId);
        return orderItemMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public boolean updateOrderStatus(Integer orderId, String status) {
        Order order = this.getById(orderId);
        if (order == null) {
            return false;
        }
        // 支付阶段：从 pending -> paid 时，从数据库正式扣减库存
        if ("paid".equals(status) && !"paid".equals(order.getStatus())) {
            List<OrderItem> orderItems = getOrderItemsByOrderId(orderId);
            for (OrderItem item : orderItems) {
                Product product = productMapper.selectById(item.getProductId());
                if (product == null || !"active".equals(product.getStatus())) {
                    throw new RuntimeException("商品不存在或已下架");
                }
                if (product.getStock() < item.getQuantity()) {
                    throw new RuntimeException("商品库存不足，支付失败");
                }
                product.setStock(product.getStock() - item.getQuantity());
                product.setUpdateTime(LocalDateTime.now());
                productMapper.updateById(product);
            }
        }
        order.setStatus(status);
        order.setUpdateTime(LocalDateTime.now());
        return this.updateById(order);
    }

    @Override
    @Transactional
    public boolean cancelOrder(Integer orderId, Long userId) {
        Order order = this.getById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            return false;
        }
        // 只有待支付或已支付状态才能取消
        if (!"pending".equals(order.getStatus()) && !"paid".equals(order.getStatus())) {
            return false;
        }
        // 仅在已支付时回补库存（因为扣减发生在支付阶段）
        if ("paid".equals(order.getStatus())) {
            List<OrderItem> orderItems = getOrderItemsByOrderId(orderId);
            for (OrderItem item : orderItems) {
                Product product = productMapper.selectById(item.getProductId());
                if (product != null) {
                    product.setStock(product.getStock() + item.getQuantity());
                    product.setUpdateTime(LocalDateTime.now());
                    productMapper.updateById(product);
                }
            }
        }
        order.setStatus("cancelled");
        order.setUpdateTime(LocalDateTime.now());
        return this.updateById(order);
    }

    @Override
    public boolean confirmOrder(Integer orderId, Long userId) {
        Order order = this.getById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            return false;
        }

        // 只有已发货状态才能确认收货
        if (!"shipped".equals(order.getStatus())) {
            return false;
        }

        order.setStatus("completed");
        order.setUpdateTime(LocalDateTime.now());
        return this.updateById(order);
    }

    @Override
    public Page<Order> getOrdersByStatus(Long userId, String status, Integer pageNum, Integer pageSize) {
        Page<Order> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId)
                .eq(Order::getStatus, status)
                .orderByDesc(Order::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    @Transactional
    public boolean shipOrderItem(Integer orderId, Integer itemId, Long sellerId) {
        Order order = this.getById(orderId);
        if (order == null) {
            return false;
        }
        // 这里暂不校验 sellerId 与商品归属关系，如有需要可根据商品 farmer_id 补充
        OrderItem item = orderItemMapper.selectById(itemId);
        if (item == null || !orderId.equals(item.getOrderId())) {
            return false;
        }
        // 只有已支付的订单项才允许发货
        if (!"PAID".equalsIgnoreCase(item.getStatus())) {
            return false;
        }
        item.setStatus("SHIPPED");
        orderItemMapper.updateById(item);
        // 订单整体状态可以根据需要调整，这里不强制改变
        return true;
    }

    @Override
    @Transactional
    public boolean confirmOrderItem(Integer orderId, Integer itemId, Long userId) {
        Order order = this.getById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            return false;
        }
        OrderItem item = orderItemMapper.selectById(itemId);
        if (item == null || !orderId.equals(item.getOrderId())) {
            return false;
        }
        // 仅已发货的订单项可以确认收货
        if (!"SHIPPED".equalsIgnoreCase(item.getStatus())) {
            return false;
        }
        item.setStatus("RECEIVED");
        orderItemMapper.updateById(item);
        return true;
    }

    @Override
    @Transactional
    public boolean applyRefundForItem(Integer orderId, Integer itemId, Long userId, String reason) {
        Order order = this.getById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            return false;
        }
        OrderItem item = orderItemMapper.selectById(itemId);
        if (item == null || !orderId.equals(item.getOrderId())) {
            return false;
        }
        // 只有已支付、已发货、已收货、已评价的订单项允许申请退款
        String status = item.getStatus();
        if (!"PAID".equalsIgnoreCase(status)
                && !"SHIPPED".equalsIgnoreCase(status)
                && !"RECEIVED".equalsIgnoreCase(status)
                && !"REVIEWED".equalsIgnoreCase(status)) {
            return false;
        }
        // 已取消/已退款/已在退款中的不再重复申请
        if ("CANCELLED".equalsIgnoreCase(status)
                || "REFUND_REQUESTED".equalsIgnoreCase(status)
                || "REFUNDED".equalsIgnoreCase(status)) {
            return false;
        }
        item.setRefundStatus("REQUESTED");
        item.setRefundReason(reason);
        item.setStatus("REFUND_REQUESTED");
        orderItemMapper.updateById(item);
        return true;
    }

    @Override
    @Transactional
    public boolean reviewRefundForItem(Integer orderId, Integer itemId, Long sellerId, boolean approve, String rejectReason) {
        Order order = this.getById(orderId);
        if (order == null) {
            return false;
        }
        OrderItem item = orderItemMapper.selectById(itemId);
        if (item == null || !orderId.equals(item.getOrderId())) {
            return false;
        }
        if (!"REQUESTED".equalsIgnoreCase(item.getRefundStatus())) {
            return false;
        }
        if (approve) {
            // 同意该订单项退款：回滚库存并标记已退款
            Product product = productMapper.selectById(item.getProductId());
            if (product != null) {
                product.setStock(product.getStock() + item.getQuantity());
                product.setUpdateTime(LocalDateTime.now());
                productMapper.updateById(product);
            }
            item.setRefundStatus("COMPLETED");
            item.setStatus("REFUNDED");
        } else {
            // 拒绝该订单项退款
            item.setRefundStatus("REJECTED");
            item.setRefundReason(rejectReason);
            // 简单回退到已支付状态，方便继续后续流程（可根据需要更细分）
            if ("REFUND_REQUESTED".equalsIgnoreCase(item.getStatus())) {
                item.setStatus("PAID");
            }
        }
        orderItemMapper.updateById(item);
        return true;
    }
}
