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
        
        // 计算总金额并验证商品
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
        
        // 创建订单
        Order order = new Order();
        order.setUserId(userId);
        order.setOrderDate(LocalDateTime.now());
        order.setTotalAmount(totalAmount);
        order.setStatus("pending"); // 待支付
        order.setShippingAddressId(addressId);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        
        if (this.save(order)) {
            // 保存订单项
            for (OrderItem item : orderItems) {
                item.setOrderId(order.getOrderId());
                item.setCreateTime(LocalDateTime.now());
                orderItemMapper.insert(item);
                
                // 扣减库存
                Product product = productMapper.selectById(item.getProductId());
                product.setStock(product.getStock() - item.getQuantity());
                product.setUpdateTime(LocalDateTime.now());
                productMapper.updateById(product);
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
        
        // 恢复库存
        List<OrderItem> orderItems = getOrderItemsByOrderId(orderId);
        for (OrderItem item : orderItems) {
            Product product = productMapper.selectById(item.getProductId());
            if (product != null) {
                product.setStock(product.getStock() + item.getQuantity());
                product.setUpdateTime(LocalDateTime.now());
                productMapper.updateById(product);
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


