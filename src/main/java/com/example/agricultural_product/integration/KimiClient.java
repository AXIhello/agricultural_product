package com.example.agricultural_product.integration;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class KimiClient { // 保留类名，内部已改为 DeepSeek

  private static final Logger log = LoggerFactory.getLogger(KimiClient.class);
  private static final String PLAIN_TEXT_INSTRUCTION =
      "请严格只输出纯文本：不要使用Markdown、不要代码块、不要反引号、不要列表或标题、不要表情符号、不要HTML标记。";

  private final RestClient restClient;
  private final String model;
  private final double temperature;
  private final int maxTokens;

  public KimiClient(
      @Value("${deepseek.base-url}") String baseUrl,
      @Value("${deepseek.api-key}") String apiKey,
      @Value("${deepseek.model:deepseek-chat}") String model,
      @Value("${deepseek.temperature:0.2}") double temperature,
      @Value("${deepseek.max-tokens:512}") int maxTokens) {
    this.restClient = RestClient.builder()
        .baseUrl(baseUrl)
        .defaultHeader("Authorization", "Bearer " + apiKey)
        .build();
    this.model = model;
    this.temperature = temperature;
    this.maxTokens = maxTokens;
  }

  public String chat(String systemPrompt, String userPrompt) {
    String mergedSystemPrompt = (systemPrompt == null || systemPrompt.isBlank())
        ? PLAIN_TEXT_INSTRUCTION
        : PLAIN_TEXT_INSTRUCTION + "\n\n" + systemPrompt;

    ChatRequest req = new ChatRequest(
        model,
        List.of(new Message("system", mergedSystemPrompt), new Message("user", userPrompt)),
        temperature,
        maxTokens,
        false,
        new ResponseFormat("text")
    );
    try {
      ChatResponse resp = restClient.post()
          .uri("/chat/completions") // DeepSeek 兼容 OpenAI 路径
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON)
          .body(req)
          .retrieve()
          .body(ChatResponse.class);

      if (resp == null || resp.choices == null || resp.choices.isEmpty()
          || resp.choices.get(0) == null || resp.choices.get(0).message == null) {
        return null;
      }
      return resp.choices.get(0).message.content;
    } catch (Exception e) {
      // 忽略任何异常，返回 null，不影响上层业务
      log.warn("调用 DeepSeek 失败，已忽略。", e);
      return null;
    }
  }

  public record ChatRequest(
      String model,
      List<Message> messages,
      Double temperature,
      @JsonProperty("max_tokens") Integer maxTokens,
      Boolean stream,
      @JsonProperty("response_format") ResponseFormat responseFormat
  ) {}

  public record ResponseFormat(String type) {}

  public record Message(String role, String content) {}

  public static class ChatResponse {
    public List<Choice> choices;
    public static class Choice { public Message message; }
    public static class Message { public String role; public String content; }
  }
}