package com.example.agricultural_product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.agricultural_product.mapper.AutoReplyRuleMapper;
import com.example.agricultural_product.mapper.ChatMessageMapper;
import com.example.agricultural_product.mapper.ChatSessionMapper;
import com.example.agricultural_product.pojo.AutoReplyRule;
import com.example.agricultural_product.pojo.ChatMessage;
import com.example.agricultural_product.pojo.ChatSession;
import com.example.agricultural_product.service.AutoReplyService;
import com.example.agricultural_product.service.ChatPushService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class AutoReplyServiceImpl implements AutoReplyService {

    private final AutoReplyRuleMapper ruleMapper;
    private final ChatMessageMapper messageMapper;
    private final ChatSessionMapper sessionMapper;
    private final ChatPushService pushService;

    public AutoReplyServiceImpl(AutoReplyRuleMapper ruleMapper,
                                ChatMessageMapper messageMapper,
                                ChatSessionMapper sessionMapper,
                                ChatPushService pushService) {
        this.ruleMapper = ruleMapper;
        this.messageMapper = messageMapper;
        this.sessionMapper = sessionMapper;
        this.pushService = pushService;
    }

    @Override
    @Transactional
    public AutoReplyRule saveOrUpdate(AutoReplyRule rule, long sellerId) {
        if (rule.getKeyword() == null || rule.getKeyword().trim().isEmpty())
            throw new IllegalArgumentException("关键词不能为空");
        if (rule.getReplyText() == null || rule.getReplyText().trim().isEmpty())
            throw new IllegalArgumentException("回复文本不能为空");

        rule.setSellerId(sellerId);
        if (rule.getEnabled() == null) rule.setEnabled(true);
        if (rule.getPriority() == null) rule.setPriority(100);
        if (rule.getMatchType() == null || rule.getMatchType().isEmpty()) rule.setMatchType("contains");

        if (rule.getRuleId() == null) {
            ruleMapper.insert(rule);
            return ruleMapper.selectById(rule.getRuleId());
        } else {
            // 确保归属
            LambdaQueryWrapper<AutoReplyRule> qw = new LambdaQueryWrapper<AutoReplyRule>()
                    .eq(AutoReplyRule::getRuleId, rule.getRuleId())
                    .eq(AutoReplyRule::getSellerId, sellerId);
            AutoReplyRule existing = ruleMapper.selectOne(qw);
            if (existing == null) throw new IllegalArgumentException("规则不存在或无权修改");
            ruleMapper.updateById(rule);
            return ruleMapper.selectById(rule.getRuleId());
        }
    }

    @Override
    @Transactional
    public boolean deleteRule(long sellerId, long ruleId) {
        return ruleMapper.delete(new LambdaQueryWrapper<AutoReplyRule>()
                .eq(AutoReplyRule::getRuleId, ruleId)
                .eq(AutoReplyRule::getSellerId, sellerId)) > 0;
    }

    @Override
    @Transactional
    public boolean toggleRule(long sellerId, long ruleId, boolean enabled) {
        AutoReplyRule r = ruleMapper.selectOne(new LambdaQueryWrapper<AutoReplyRule>()
                .eq(AutoReplyRule::getRuleId, ruleId)
                .eq(AutoReplyRule::getSellerId, sellerId));
        if (r == null) return false;
        r.setEnabled(enabled);
        return ruleMapper.updateById(r) > 0;
    }

    @Override
    public List<AutoReplyRule> listMyRules(long sellerId) {
        return ruleMapper.selectList(new LambdaQueryWrapper<AutoReplyRule>()
                .eq(AutoReplyRule::getSellerId, sellerId)
                .orderByAsc(AutoReplyRule::getPriority)
                .orderByDesc(AutoReplyRule::getUpdateTime));
    }

    @Override
    @Transactional
    public void tryAutoReplyForReceiver(long receiverId, long sessionId, long senderId, String incomingContent) {
        if (incomingContent == null || incomingContent.isEmpty()) return;

        List<AutoReplyRule> rules = ruleMapper.selectList(new LambdaQueryWrapper<AutoReplyRule>()
                .eq(AutoReplyRule::getSellerId, receiverId)
                .eq(AutoReplyRule::getEnabled, true));

        if (rules == null || rules.isEmpty()) return;

        // 优先级排序
        rules.sort(Comparator.comparingInt(r -> r.getPriority() == null ? 100 : r.getPriority()));

        String content = incomingContent.toLowerCase();

        for (AutoReplyRule r : rules) {
            String type = r.getMatchType() == null ? "contains" : r.getMatchType();
            String kw = r.getKeyword();
            if (kw == null || kw.isEmpty()) continue;

            boolean match = false;
            switch (type) {
                case "exact":
                    match = content.equals(kw.toLowerCase());
                    break;
                case "regex":
                    try {
                        match = Pattern.compile(kw, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE).matcher(incomingContent).find();
                    } catch (Exception ignored) {}
                    break;
                case "contains":
                default:
                    match = content.contains(kw.toLowerCase());
            }

            if (match) {
                // 发送自动回复（由接收方回复给发送方）
                ChatMessage auto = new ChatMessage();
                auto.setSessionId(sessionId);
                auto.setSenderId(receiverId);
                auto.setReceiverId(senderId);
                auto.setContent(r.getReplyText());
                auto.setMsgType("auto");
                auto.setSendTime(LocalDateTime.now());
                messageMapper.insert(auto);

                // 更新会话最后时间
                sessionMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<ChatSession>()
                        .eq(ChatSession::getSessionId, sessionId)
                        .set(ChatSession::getLastMessageTime, LocalDateTime.now()));

                ChatMessage saved = messageMapper.selectById(auto.getMessageId());
                // 推送给双方
                pushService.notifyNewMessage(saved);
                break; // 命中第一条即返回
            }
        }
    }
}