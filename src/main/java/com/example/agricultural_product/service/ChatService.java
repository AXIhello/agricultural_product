package com.example.agricultural_product.service;

import com.example.agricultural_product.pojo.ChatMessage;
import com.example.agricultural_product.pojo.ChatSession;

import java.util.List;

public interface ChatService {
    ChatSession createOrGetSession(long currentUserId, long peerUserId);
    ChatMessage sendMessage(long currentUserId, Long sessionId, Long peerUserId, String content, String msgType);
    List<ChatMessage> listMessages(long currentUserId, long sessionId, int page, int size);
    List<ChatSession> mySessions(long currentUserId);
    void markMessageRead(long currentUserId, long messageId);
}