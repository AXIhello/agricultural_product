package com.example.agricultural_product.dto;

import lombok.Data;

@Data
public class AnswerQuestionRequest {
	private Long expertId;
	private String answerContent;
} 