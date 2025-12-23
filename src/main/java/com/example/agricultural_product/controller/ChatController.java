package com.example.agricultural_product.controller;

import com.example.agricultural_product.pojo.ChatMessage;
import com.example.agricultural_product.pojo.ChatSession;
import com.example.agricultural_product.service.ChatPushService;
import com.example.agricultural_product.service.ChatService;
import com.example.agricultural_product.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;
    private final ChatPushService pushService;

    public ChatController(ChatService chatService, ChatPushService pushService) {
        this.chatService = chatService;
        this.pushService = pushService;
    }

    // SSE 实时连接（前端使用 EventSource('/api/chat/stream?token=...')）
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(HttpServletRequest request) {
        long uid = currentUserId(request);
        return pushService.connect(uid);
    }

    @PostMapping("/session/{peerId}")
    public ChatSession createOrGetSession(@PathVariable("peerId") Long peerId,
                                          @RequestBody(required = false) SessionRequest body,
                                          HttpServletRequest request) {
        long uid = currentUserId(request);
        String currentRole = body == null ? null : body.getCurrentRole();
        String peerRole = body == null ? null : body.getPeerRole();
        return chatService.createOrGetSession(uid, peerId, currentRole, peerRole);
    }

    @GetMapping("/sessions")
    public List<ChatSession> mySessions(HttpServletRequest request) {
        long uid = currentUserId(request);
        return chatService.mySessions(uid);
    }

    @GetMapping("/messages/{sessionId}")
    public List<ChatMessage> listMessages(@PathVariable("sessionId") Long sessionId,
                                          @RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "20") int size,
                                          HttpServletRequest request) {
        long uid = currentUserId(request);
        return chatService.listMessages(uid, sessionId, page, size);
    }

    @PostMapping("/messages")
    public ChatMessage send(@RequestBody SendMessageRequest req, HttpServletRequest request) {
        long uid = currentUserId(request);
        return chatService.sendMessage(uid, req.getSessionId(), req.getPeerUserId(), req.getContent(), req.getMsgType(),
                req.getCurrentRole(), req.getPeerRole());
    }

    @PutMapping("/messages/{messageId}/read")
    public void markRead(@PathVariable("messageId") Long messageId, HttpServletRequest request) {
        long uid = currentUserId(request);
        chatService.markMessageRead(uid, messageId);
    }

    // 从 request 中解析当前用户ID：优先使用 JwtFilter 预先放入的属性；否则回退解析 header/param。
    private long currentUserId(HttpServletRequest request) {
        Object attr = request.getAttribute("userId");
        if (attr != null) {
            if (attr instanceof Long) return (Long) attr;
            return Long.parseLong(String.valueOf(attr));
        }
        String token = null;
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            token = auth.substring(7);
        }
        if (token == null || token.isEmpty()) {
            token = request.getParameter("token");
        }
        if (token == null || token.isEmpty()) {
            throw new RuntimeException("未认证");
        }
        return JwtUtil.getUserId(token);
    }

    public static class SendMessageRequest {
        private Long sessionId;   // 可选
        private Long peerUserId;  // 可选
        private String content;
        private String msgType;   // text/image
        private String currentRole;
        private String peerRole;

        public Long getSessionId() { return sessionId; }
        public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
        public Long getPeerUserId() { return peerUserId; }
        public void setPeerUserId(Long peerUserId) { this.peerUserId = peerUserId; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getMsgType() { return msgType; }
        public void setMsgType(String msgType) { this.msgType = msgType; }
        public String getCurrentRole() { return currentRole; }
        public void setCurrentRole(String currentRole) { this.currentRole = currentRole; }
        public String getPeerRole() { return peerRole; }
        public void setPeerRole(String peerRole) { this.peerRole = peerRole; }
    }

    public static class SessionRequest {
        private String currentRole;
        private String peerRole;

        public String getCurrentRole() { return currentRole; }
        public void setCurrentRole(String currentRole) { this.currentRole = currentRole; }
        public String getPeerRole() { return peerRole; }
        public void setPeerRole(String peerRole) { this.peerRole = peerRole; }
    }
}