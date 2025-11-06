package com.example.agricultural_product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AgriculturalProductApplication {
    public static void main(String[] args) {
        SpringApplication.run(AgriculturalProductApplication.class, args);
    }
}
