package com.example.agricultural_product.service.impl;

import com.example.agricultural_product.pojo.ChatMessage;
import com.example.agricultural_product.service.ChatPushService;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class SseChatPushService implements ChatPushService {
    
    //添加logger用于记录日志
    private static final Logger logger = LoggerFactory.getLogger(SseChatPushService.class);

    private static final long DEFAULT_TIMEOUT = 30 * 60 * 1000L; // 30min
    private final Map<Long, Set<SseEmitter>> clients = new ConcurrentHashMap<>();


     //添加一个定时任务执行器，用于心跳
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    //在构造函数中启动心跳任务
    public SseChatPushService() {
        // 每隔20秒，执行一次 sendHeartbeat 方法
        scheduler.scheduleAtFixedRate(this::sendHeartbeat, 20, 20, TimeUnit.SECONDS);
        logger.info("SSE Heartbeat scheduler started.");
    }

    
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
            logger.info("New SSE connection for user: {}", userId);
        } catch (Exception e) { 
            logger.warn("Failed to send initial event for user: {}, removing emitter.", userId, e);
            remove(userId, emitter); // 发送失败，立刻移除
        }
        return emitter;
    }

    @Override
    public void notifyNewMessage(ChatMessage message) {
        // 推送给接收者
        broadcast(message.getReceiverId(), "message", message);
        // 同步给发送者（用于对端回显）
        broadcast(message.getSenderId(), "message", message);
    }

     //心跳方法
    private void sendHeartbeat() {
        // 遍历所有在线的用户
        for (long userId : clients.keySet()) {
            // 向该用户的所有客户端发送心跳
            broadcast(userId, "heartbeat", "ping");
        }
    }

    private void broadcast(long userId, String event, Object payload) {
        Set<SseEmitter> set = clients.get(userId);
        if (set == null || set.isEmpty()) return;
        List<SseEmitter> dead = new ArrayList<>();
        List<SseEmitter> deadEmitters = new ArrayList<>();
        for (SseEmitter emitter : set) {
            try {
                emitter.send(SseEmitter.event().name(event).data(payload));
            } catch (Exception ex) {
                dead.add(emitter);
                logger.warn("Broadcast failed for user: {}. Emitter will be removed.", userId);
            }
        }
        if (!dead.isEmpty()) {
            set.removeAll(dead);
        }
        if (set.isEmpty()) {
            clients.remove(userId);
        }
         if (!deadEmitters.isEmpty()) {
            deadEmitters.forEach(emitter -> remove(userId, emitter));
        }
    }

    private void remove(long userId, SseEmitter emitter) {
        Set<SseEmitter> set = clients.get(userId);
        if (set == null) return;
        set.remove(emitter);
        if (set.isEmpty()) {
            clients.remove(userId);
            logger.info("All emitters removed for user: {}", userId);
        }
    }
}