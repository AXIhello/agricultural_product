package com.example.agricultural_product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.agricultural_product.mapper.OrderItemMapper;
import com.example.agricultural_product.mapper.OrderItemReviewMapper;
import com.example.agricultural_product.mapper.OrderMapper;
import com.example.agricultural_product.mapper.ProductMapper;
import com.example.agricultural_product.pojo.Order;
import com.example.agricultural_product.pojo.OrderItem;
import com.example.agricultural_product.pojo.OrderItemReview;
import com.example.agricultural_product.pojo.Product;
import com.example.agricultural_product.service.OrderItemReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderItemReviewServiceImpl implements OrderItemReviewService {

    private final OrderItemReviewMapper orderItemReviewMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderItemReview createReview(Long userId, Integer orderId, Integer itemId,
                                        Integer productId, Integer rating, String content,
                                        Boolean isAnonymous) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("评分必须在1到5之间");
        }
        if (Boolean.FALSE.equals(isAnonymous)) {
            // 允许 null 表示不传则默认非匿名，这里不做额外处理
        }

        // 校验订单
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new IllegalArgumentException("订单不存在或不属于当前用户");
        }
        // 可以根据你的项目约定，限制只有已完成的订单才能评价
        // 示例：if (!"completed".equals(order.getStatus())) { ... }

        // 校验订单项
        OrderItem orderItem = orderItemMapper.selectById(itemId);
        if (orderItem == null || !orderItem.getOrderId().equals(orderId)) {
            throw new IllegalArgumentException("订单明细不存在或不属于该订单");
        }
        if (!orderItem.getProductId().equals(productId)) {
            throw new IllegalArgumentException("订单明细对应的商品与传入的商品不一致");
        }
        if (orderItem.getIsReviewed() != null && orderItem.getIsReviewed() == 1) {
            throw new IllegalStateException("该订单明细已评价，不能重复评价");
        }

        // 防止重复插入评价记录
        Long count = orderItemReviewMapper.selectCount(new LambdaQueryWrapper<OrderItemReview>()
                .eq(OrderItemReview::getItemId, itemId)
                .eq(OrderItemReview::getUserId, userId));
        if (count != null && count > 0) {
            throw new IllegalStateException("该订单明细已存在评价记录");
        }

        // 构造评价实体
        OrderItemReview review = new OrderItemReview();
        review.setItemId(itemId);
        review.setOrderId(orderId);
        review.setProductId(productId);
        review.setUserId(userId);
        review.setRating(rating);
        review.setContent(content);
        review.setIsAnonymous(Boolean.TRUE.equals(isAnonymous) ? 1 : 0);
        review.setStatus("published");

        orderItemReviewMapper.insert(review);

        // 更新订单项已评价标记和状态
        orderItem.setIsReviewed(1);
        if ("RECEIVED".equalsIgnoreCase(orderItem.getStatus()) || "PAID".equalsIgnoreCase(orderItem.getStatus())) {
            // 如果已经收货或已支付（未单独维护收货），将状态标记为已评价
            orderItem.setStatus("REVIEWED");
        }
        orderItemMapper.updateById(orderItem);

        // 更新商品的评分统计（简单增量计算）
        Product product = productMapper.selectById(productId);
        if (product != null) {
            Integer ratingCount = product.getRatingCount() == null ? 0 : product.getRatingCount();
            Double averageRating = product.getAverageRating() == null ? 0.0 : product.getAverageRating();

            double newAvg = (averageRating * ratingCount + rating) / (ratingCount + 1);
            product.setRatingCount(ratingCount + 1);
            product.setAverageRating(Math.round(newAvg * 10.0) / 10.0); // 保留1位小数
            productMapper.updateById(product);
        }

        return review;
    }

    @Override
    public Page<OrderItemReview> listProductReviews(Integer productId, long pageNum, long pageSize) {
        Page<OrderItemReview> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<OrderItemReview> wrapper = new LambdaQueryWrapper<OrderItemReview>()
                .eq(OrderItemReview::getProductId, productId)
                .eq(OrderItemReview::getStatus, "published")
                .orderByDesc(OrderItemReview::getCreateTime);
        orderItemReviewMapper.selectPage(page, wrapper);
        return page;
    }

    @Override
    public OrderItemReview getByItemId(Integer itemId) {
        // 使用 MyBatis-Plus 的 LambdaQueryWrapper 根据 item_id 精确查询
        LambdaQueryWrapper<OrderItemReview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItemReview::getItemId, itemId);
        
        // selectOne 返回单条记录
        return orderItemReviewMapper.selectOne(wrapper);
    }
}
