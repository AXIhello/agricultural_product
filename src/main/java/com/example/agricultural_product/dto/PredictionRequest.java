package com.example.agricultural_product.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PredictionRequest {
    private String productName;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private String status;
    private String imagePath;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String prodCat;
    private String prodPcat;
    private String unitInfo;
    private String specInfo;
    private String place;
    private String labelPre;    //预测标志-若为“预测”则进入预测，否则只更新或者存储


}