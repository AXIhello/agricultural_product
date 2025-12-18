package com.example.agricultural_product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.agricultural_product.pojo.OrderItemReview;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OrderItemReviewMapper extends BaseMapper<OrderItemReview> {

    // 计算平均分
    @Select("SELECT AVG(rating) FROM tb_order_item_review WHERE product_id = #{productId}")
    Double getAverageRatingByProductId(Integer productId);

    // 计算评价数
    @Select("SELECT COUNT(*) FROM tb_order_item_review WHERE product_id = #{productId}")
    Integer getCountByProductId(Integer productId);

}
