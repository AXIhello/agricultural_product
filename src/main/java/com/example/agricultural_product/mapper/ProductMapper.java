package com.example.agricultural_product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.agricultural_product.pojo.Product;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Repository;
import org.mybatis.spring.annotation.MapperScan;



@Repository
public interface ProductMapper extends BaseMapper<Product> {

}