package com.example.agricultural_product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.agricultural_product.mapper.OrderItemMapper;
import com.example.agricultural_product.mapper.OrderMapper;
import com.example.agricultural_product.mapper.ProductMapper;
import com.example.agricultural_product.pojo.Order;
import com.example.agricultural_product.pojo.OrderItem;
import com.example.agricultural_product.pojo.Product;
import com.example.agricultural_product.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Override
    @Transactional
    public Integer createOrder(Long userId, Integer addressId, List<OrderItem> orderItems) {
        if (userId == null || addressId == null || orderItems == null || orderItems.isEmpty()) {
            return null;
        }
        // 计算总金额并验证商品（仅检查，不扣减）
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItem item : orderItems) {
            Product product = productMapper.selectById(item.getProductId());
            if (product == null || !"active".equals(product.getStatus())) {
                throw new RuntimeException("商品不存在或已下架");
            }
            if (product.getStock() < item.getQuantity()) {
                throw new RuntimeException("商品库存不足");
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

    // @Override
    // public Page<Order> getOrdersByUserId(Long userId, Integer pageNum, Integer pageSize) {
    //     Page<Order> page = new Page<>(pageNum, pageSize);
    //     LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
    //     wrapper.eq(Order::getUserId, userId)
    //            .orderByDesc(Order::getCreateTime);
    //     return this.page(page, wrapper);
    // }

    @Override
    public Page<Order> getOrdersByUserId(Long userId, Integer pageNum, Integer pageSize) {
    Page<Order> page = new Page<>(pageNum, pageSize);
    
    return this.baseMapper.selectOrderPageWithAddress(page, userId);
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
        // 支付阶段：从 pending -> paid 时再扣减库存（再次检查防止并发超卖）
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
}

