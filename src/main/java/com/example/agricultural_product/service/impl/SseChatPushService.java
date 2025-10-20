package com.example.agricultural_product.service.impl;

import com.example.agricultural_product.pojo.ChatMessage;
import com.example.agricultural_product.service.ChatPushService;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SseChatPushService implements ChatPushService {

    private static final long DEFAULT_TIMEOUT = 30 * 60 * 1000L; // 30min
    private final Map<Long, Set<SseEmitter>> clients = new ConcurrentHashMap<>();

    @Override
    public SseEmitter connect(long userId) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        clients.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(emitter);
        emitter.onCompletion(() -> remove(userId, emitter));
        emitter.onTimeout(() -> remove(userId, emitter));
        emitter.onError(ex -> remove(userId, emitter));
        // 首包
        try {
            emitter.send(SseEmitter.event().name("init").data("ok"));
        } catch (Exception ignored) { }
        return emitter;
    }

    @Override
    public void notifyNewMessage(ChatMessage message) {
        // 推送给接收者
        broadcast(message.getReceiverId(), "message", message);
        // 同步给发送者（用于对端回显）
        broadcast(message.getSenderId(), "message", message);
    }

    private void broadcast(long userId, String event, Object payload) {
        Set<SseEmitter> set = clients.get(userId);
        if (set == null || set.isEmpty()) return;
        List<SseEmitter> dead = new ArrayList<>();
        for (SseEmitter emitter : set) {
            try {
                emitter.send(SseEmitter.event().name(event).data(payload));
            } catch (Exception ex) {
                dead.add(emitter);
            }
        }
        if (!dead.isEmpty()) {
            set.removeAll(dead);
        }
        if (set.isEmpty()) {
            clients.remove(userId);
        }
    }

    private void remove(long userId, SseEmitter emitter) {
        Set<SseEmitter> set = clients.get(userId);
        if (set == null) return;
        set.remove(emitter);
        if (set.isEmpty()) {
            clients.remove(userId);
        }
    }
}