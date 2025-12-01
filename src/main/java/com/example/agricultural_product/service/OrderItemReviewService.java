package com.example.agricultural_product.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.agricultural_product.pojo.OrderItemReview;

public interface OrderItemReviewService {

    /**
     * 针对某个订单项创建评价（首评），内部会做订单归属和状态校验。
     */
    OrderItemReview createReview(Long userId, Integer orderId, Integer itemId,
                                 Integer productId, Integer rating, String content,
                                 Boolean isAnonymous);

    /**
     * 分页查询某个商品的评价列表（只返回已发布的评价）。
     */
    Page<OrderItemReview> listProductReviews(Integer productId, long pageNum, long pageSize);
}
