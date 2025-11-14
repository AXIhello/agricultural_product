package com.example.agricultural_product.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AnswerVO {

    // ExpertAnswer 的所有字段
    private Integer answerId;
    private Integer questionId;
    private Long responderId;
    private String content;
    private Integer isAccepted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 从users表关联查询出的字段
    private String responderName;
}