package com.example.agricultural_product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.agricultural_product.mapper.ChatMessageMapper;
import com.example.agricultural_product.mapper.ChatSessionMapper;
import com.example.agricultural_product.pojo.ChatMessage;
import com.example.agricultural_product.pojo.ChatSession;
import com.example.agricultural_product.service.ChatPushService;
import com.example.agricultural_product.service.ChatService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatServiceImpl implements ChatService {

    private final ChatSessionMapper sessionMapper;
    private final ChatMessageMapper messageMapper;
    private final ChatPushService pushService;

    public ChatServiceImpl(ChatSessionMapper sessionMapper,
                           ChatMessageMapper messageMapper,
                           ChatPushService pushService) {
        this.sessionMapper = sessionMapper;
        this.messageMapper = messageMapper;
        this.pushService = pushService;
    }

    private void assertParticipant(long sessionId, long userId) {
        Long cnt = sessionMapper.selectCount(
                new LambdaQueryWrapper<ChatSession>()
                        .eq(ChatSession::getSessionId, sessionId)
                        .and(w -> w.eq(ChatSession::getUserAId, userId)
                                   .or()
                                   .eq(ChatSession::getUserBId, userId))
        );
        if (cnt == null || cnt == 0) throw new IllegalArgumentException("无权访问该会话");
    }

    @Override
    @Transactional
    public ChatSession createOrGetSession(long currentUserId, long peerUserId) {
        if (currentUserId == peerUserId) throw new IllegalArgumentException("不能与自己创建会话");
        long a = Math.min(currentUserId, peerUserId);
        long b = Math.max(currentUserId, peerUserId);

        ChatSession exist = sessionMapper.selectOne(
                new LambdaQueryWrapper<ChatSession>()
                        .eq(ChatSession::getUserAId, a)
                        .eq(ChatSession::getUserBId, b)
        );
        if (exist != null) return exist;

        ChatSession s = new ChatSession();
        s.setUserAId(a);
        s.setUserBId(b);
        sessionMapper.insert(s);
        return sessionMapper.selectById(s.getSessionId());
    }

    @Override
    @Transactional
    public ChatMessage sendMessage(long currentUserId, Long sessionId, Long peerUserId, String content, String msgType) {
        if (content == null || content.trim().isEmpty()) throw new IllegalArgumentException("消息内容不能为空");
        String type = (msgType == null || msgType.isEmpty()) ? "text" : msgType;

        Long sid = sessionId;
        if (sid == null) {
            if (peerUserId == null) throw new IllegalArgumentException("缺少会话或对端ID");
            sid = createOrGetSession(currentUserId, peerUserId).getSessionId();
        } else {
            assertParticipant(sid, currentUserId);
        }

        ChatSession s = sessionMapper.selectById(sid);
        if (s == null) throw new IllegalArgumentException("会话不存在");
        long other = s.getUserAId().equals(currentUserId) ? s.getUserBId() : s.getUserAId();

        ChatMessage m = new ChatMessage();
        m.setSessionId(sid);
        m.setSenderId(currentUserId);
        m.setReceiverId(other);
        m.setContent(content);
        m.setMsgType(type);
        messageMapper.insert(m);

        sessionMapper.update(null, new LambdaUpdateWrapper<ChatSession>()
                .eq(ChatSession::getSessionId, sid)
                .set(ChatSession::getLastMessageTime, LocalDateTime.now()));

        ChatMessage saved = messageMapper.selectById(m.getMessageId());
        pushService.notifyNewMessage(saved);
        return saved;
    }

    @Override
    public List<ChatMessage> listMessages(long currentUserId, long sessionId, int page, int size) {
        assertParticipant(sessionId, currentUserId);
        int p = Math.max(page, 1);
        int s = Math.max(Math.min(size, 100), 1);
        Page<ChatMessage> pg = new Page<>(p, s);
        messageMapper.selectPage(pg, new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getSessionId, sessionId)
                .orderByAsc(ChatMessage::getSendTime)
                .orderByAsc(ChatMessage::getMessageId));
        return pg.getRecords();
    }

    @Override
    public List<ChatSession> mySessions(long currentUserId) {
        return sessionMapper.selectList(new LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getUserAId, currentUserId)
                .or()
                .eq(ChatSession::getUserBId, currentUserId)
                .orderByDesc(ChatSession::getLastMessageTime));
    }

    @Override
    public void markMessageRead(long currentUserId, long messageId) {
        int updated = messageMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<ChatMessage>()
                .eq(ChatMessage::getMessageId, messageId)
                .eq(ChatMessage::getReceiverId, currentUserId)
                .set(ChatMessage::getRead, true));
        if (updated == 0) throw new IllegalArgumentException("消息不存在或无权标记");
    }
}