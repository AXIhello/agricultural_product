package com.example.agricultural_product.service.impl;

import com.example.agricultural_product.pojo.ChatMessage;
import com.example.agricultural_product.pojo.ChatSession;
import com.example.agricultural_product.service.ChatPushService;
import com.example.agricultural_product.service.ChatService;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatServiceImpl implements ChatService {

    private final JdbcTemplate jdbc;
    private final ChatPushService pushService;

    public ChatServiceImpl(JdbcTemplate jdbc, ChatPushService pushService) {
        this.jdbc = jdbc;
        this.pushService = pushService;
    }

    private ChatSession mapSession(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        ChatSession s = new ChatSession();
        s.setSessionId(rs.getLong("session_id"));
        s.setUserAId(rs.getLong("user_a_id"));
        s.setUserBId(rs.getLong("user_b_id"));
        s.setLastMessageTime(rs.getTimestamp("last_message_time").toLocalDateTime());
        s.setCreateTime(rs.getTimestamp("create_time").toLocalDateTime());
        s.setUpdateTime(rs.getTimestamp("update_time").toLocalDateTime());
        return s;
    }

    private ChatMessage mapMessage(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        ChatMessage m = new ChatMessage();
        m.setMessageId(rs.getLong("message_id"));
        m.setSessionId(rs.getLong("session_id"));
        m.setSenderId(rs.getLong("sender_id"));
        m.setReceiverId(rs.getLong("receiver_id"));
        m.setContent(rs.getString("content"));
        m.setMsgType(rs.getString("msg_type"));
        m.setRead(rs.getBoolean("is_read"));
        m.setSendTime(rs.getTimestamp("send_time").toLocalDateTime());
        return m;
    }

    private void assertParticipant(long sessionId, long userId) {
        Integer c = jdbc.queryForObject(
                "SELECT COUNT(1) FROM tb_chat_session WHERE session_id=? AND (user_a_id=? OR user_b_id=?)",
                Integer.class, sessionId, userId, userId);
        if (c == null || c == 0) {
            throw new IllegalArgumentException("无权访问该会话");
        }
    }

    @Override
    @Transactional
    public ChatSession createOrGetSession(long currentUserId, long peerUserId) {
        if (currentUserId == peerUserId) {
            throw new IllegalArgumentException("不能与自己创建会话");
        }
        long a = Math.min(currentUserId, peerUserId);
        long b = Math.max(currentUserId, peerUserId);
        try {
            return jdbc.queryForObject(
                    "SELECT * FROM tb_chat_session WHERE user_a_id=? AND user_b_id=?",
                    this::mapSession, a, b);
        } catch (EmptyResultDataAccessException e) {
            KeyHolder kh = new GeneratedKeyHolder();
            String sql = "INSERT INTO tb_chat_session (user_a_id, user_b_id) VALUES (?, ?)";
            jdbc.update(con -> {
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, a);
                ps.setLong(2, b);
                return ps;
            }, kh);
            Long id = kh.getKey().longValue();
            return jdbc.queryForObject("SELECT * FROM tb_chat_session WHERE session_id=?", this::mapSession, id);
        }
    }

    @Override
    @Transactional
    public ChatMessage sendMessage(long currentUserId, Long sessionId, Long peerUserId, String content, String msgType) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("消息内容不能为空");
        }
        String type = (msgType == null || msgType.isEmpty()) ? "text" : msgType;

        Long sid = sessionId;
        if (sid == null) {
            if (peerUserId == null) throw new IllegalArgumentException("缺少会话或对端ID");
            ChatSession s = createOrGetSession(currentUserId, peerUserId);
            sid = s.getSessionId();
        } else {
            assertParticipant(sid, currentUserId);
        }

        // 供 lambda 使用的最终变量，避免“lambda 使用的变量必须是最终或实际最终”错误
        final long sessionIdFinal = sid;

        ChatSession s = jdbc.queryForObject(
                "SELECT * FROM tb_chat_session WHERE session_id=?",
                this::mapSession, sessionIdFinal);

        long other = (s.getUserAId().equals(currentUserId)) ? s.getUserBId() : s.getUserAId();

        KeyHolder kh = new GeneratedKeyHolder();
        String insertSql = "INSERT INTO tb_chat_message (session_id, sender_id, receiver_id, content, msg_type) VALUES (?,?,?,?,?)";
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, sessionIdFinal);
            ps.setLong(2, currentUserId);
            ps.setLong(3, other);
            ps.setString(4, content);
            ps.setString(5, type);
            return ps;
        }, kh);

        jdbc.update("UPDATE tb_chat_session SET last_message_time=? WHERE session_id=?",
                java.sql.Timestamp.valueOf(LocalDateTime.now()), sessionIdFinal);

        Long mid = kh.getKey().longValue();
        ChatMessage saved = jdbc.queryForObject(
                "SELECT * FROM tb_chat_message WHERE message_id=?",
                this::mapMessage, mid);

        // 实时推送给收发双方
        pushService.notifyNewMessage(saved);
        return saved;
    }

    @Override
    public List<ChatMessage> listMessages(long currentUserId, long sessionId, int page, int size) {
        assertParticipant(sessionId, currentUserId);
        int p = Math.max(page, 1);
        int s = Math.max(Math.min(size, 100), 1);
        int offset = (p - 1) * s;
        return jdbc.query(
                "SELECT * FROM tb_chat_message WHERE session_id=? ORDER BY send_time ASC, message_id ASC LIMIT ? OFFSET ?",
                this::mapMessage, sessionId, s, offset);
    }

    @Override
    public List<ChatSession> mySessions(long currentUserId) {
        return jdbc.query(
                "SELECT * FROM tb_chat_session WHERE user_a_id=? OR user_b_id=? ORDER BY last_message_time DESC",
                this::mapSession, currentUserId, currentUserId);
    }

    @Override
    public void markMessageRead(long currentUserId, long messageId) {
        int updated = jdbc.update(
                "UPDATE tb_chat_message SET is_read=1 WHERE message_id=? AND receiver_id=?",
                messageId, currentUserId);
        if (updated == 0) {
            throw new IllegalArgumentException("消息不存在或无权标记");
        }
    }
}