package com.example.agricultural_product.service;

import com.example.agricultural_product.pojo.ChatMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface ChatPushService {
    SseEmitter connect(long userId);
    void notifyNewMessage(ChatMessage message);
}