package com.example.agricultural_product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync; // 异步预测功能所需
import org.springframework.scheduling.annotation.EnableScheduling;


@MapperScan("com.example.agricultural_product.mapper")
@SpringBootApplication
@EnableAsync // 启用异步执行，支持 PricePredictionServiceImpl 中的 @Async 方法
@EnableScheduling // 启用定时任务，如您有需要
public class AgriculturalProductApplication {
    public static void main(String[] args) {
        SpringApplication.run(AgriculturalProductApplication.class, args);
    }
}