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
public class KimiClient {

  private static final Logger log = LoggerFactory.getLogger(KimiClient.class);

  private final RestClient restClient;
  private final String model;
  private final double temperature;
  private final int maxTokens;

  public KimiClient(
      @Value("${moonshot.base-url}") String baseUrl,
      @Value("${moonshot.api-key}") String apiKey,
      @Value("${moonshot.model:moonshot-v1-8k}") String model,
      @Value("${moonshot.temperature:0.2}") double temperature,
      @Value("${moonshot.max-tokens:512}") int maxTokens) {
    this.restClient = RestClient.builder()
        .baseUrl(baseUrl)
        .defaultHeader("Authorization", "Bearer " + apiKey)
        .build();
    this.model = model;
    this.temperature = temperature;
    this.maxTokens = maxTokens;
  }

  public String chat(String systemPrompt, String userPrompt) {
    ChatRequest req = new ChatRequest(
        model,
        List.of(new Message("system", systemPrompt), new Message("user", userPrompt)),
        temperature,
        maxTokens,
        false
    );
    try {
      ChatResponse resp = restClient.post()
          .uri("/chat/completions")
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
      log.warn("调用 Kimi 失败，已忽略。", e);
      return null;
    }
  }

  public record ChatRequest(
      String model,
      List<Message> messages,
      Double temperature,
      @JsonProperty("max_tokens") Integer maxTokens,
      Boolean stream
  ) {}

  public record Message(String role, String content) {}

  public static class ChatResponse {
    public List<Choice> choices;
    public static class Choice { public Message message; }
    public static class Message { public String role; public String content; }
  }
}