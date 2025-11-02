package com.example.agricultural_product.service;

import com.example.agricultural_product.pojo.ExpertQuestion;

public interface AiAnswerService {
  void generateForQuestion(ExpertQuestion question);
}