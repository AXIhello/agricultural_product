package com.example.agricultural_product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.agricultural_product.pojo.Product;
import org.apache.ibatis.annotations.Select;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.mybatis.spring.annotation.MapperScan;

import java.util.List;


@Repository
public interface ProductMapper extends BaseMapper<Product> {
    // 返回按 product_name 去重后的 Product 列表（每个名字只保留一条代表性记录）

    @Select("""
        SELECT *
        FROM (
            SELECT *,
                   ROW_NUMBER() OVER (PARTITION BY product_name ORDER BY update_time DESC, product_id DESC) AS rn
            FROM tb_product
            WHERE status = #{status}
        ) t
        WHERE t.rn = 1
        ORDER BY update_time DESC
        LIMIT #{offset}, #{pageSize}
        """)
    List<Product> selectDistinctProductsByStatus(@Param("status") String status,
                                                 @Param("offset") int offset,
                                                 @Param("pageSize") int pageSize);

    @Select("SELECT COUNT(*) FROM (" +
            "SELECT product_name FROM tb_product WHERE status = #{status} GROUP BY product_name" +
            ") temp")
    int countDistinctProductsByStatus(@Param("status") String status);

    @Select("""
        SELECT *
        FROM (
            SELECT *,
                   ROW_NUMBER() OVER (
                       PARTITION BY product_name 
                       ORDER BY update_time DESC, product_id DESC
                   ) AS rn
            FROM tb_product
            WHERE farmer_id = #{farmerId} AND status = #{status}
        ) t
        WHERE t.rn = 1
        ORDER BY update_time DESC
        LIMIT #{offset}, #{pageSize}
        """)
    List<Product> selectDistinctProductsByFarmerIdAndStatus(@Param("farmerId") Long farmerId,
                                                            @Param("status") String status,
                                                            @Param("offset") int offset,
                                                            @Param("pageSize") int pageSize);

    @Select("SELECT COUNT(*) FROM (" +
            "SELECT product_name FROM tb_product WHERE farmer_id = #{farmerId} AND status = #{status} GROUP BY product_name" +
            ") temp")
    int countDistinctProductsByFarmerIdAndStatus(@Param("farmerId") Long farmerId,
                                                 @Param("status") String status);


}