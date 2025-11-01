package com.example.agricultural_product.service.impl;

import com.example.agricultural_product.integration.KimiClient;
import com.example.agricultural_product.pojo.ExpertAnswer;
import com.example.agricultural_product.pojo.ExpertQuestion;
import com.example.agricultural_product.service.AiAnswerService;
import com.example.agricultural_product.service.ExpertAnswerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AiAnswerServiceImpl implements AiAnswerService {
  private static final Logger log = LoggerFactory.getLogger(AiAnswerServiceImpl.class);

  private final KimiClient kimiClient;
  private final ExpertAnswerService expertAnswerService;

  @Value("${moonshot.responder-id}")
  private Long aiResponderId; // 确保该用户已存在

  public AiAnswerServiceImpl(KimiClient kimiClient, ExpertAnswerService expertAnswerService) {
    this.kimiClient = kimiClient;
    this.expertAnswerService = expertAnswerService;
  }

  @Async("aiExecutor")
  @Override
  public void generateForQuestion(ExpertQuestion q) {
    try {
      if (q == null || q.getQuestionId() == null) return;
      if (aiResponderId == null) {
        log.warn("AI responderId 未配置，跳过生成。questionId={}", q.getQuestionId());
        return;
      }

      String title = q.getTitle() == null ? "" : q.getTitle();
      String detail = q.getContent() == null ? "" : q.getContent();
      String userPrompt = "请用中文、面向农户、给出可操作的建议，尽量简洁。\n"
          + "问题标题：" + title + "\n"
          + "问题详情：" + detail;
      String systemPrompt = "你是农业技术专家，回答务实可落地，避免无依据的结论。";

      String reply = kimiClient.chat(systemPrompt, userPrompt);
      if (reply == null || reply.isBlank()) return;

      ExpertAnswer ai = new ExpertAnswer();
      ai.setQuestionId(q.getQuestionId());
      ai.setResponderId(aiResponderId);
      ai.setContent(reply);
      ai.setIsAccepted(0);
      expertAnswerService.save(ai);
    } catch (Exception e) {
      // 任何异常都吞掉，绝不影响发布成功
      log.warn("生成 AI 回答失败，已忽略。questionId={}", q != null ? q.getQuestionId() : null, e);
    }
  }
}