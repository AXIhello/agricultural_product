package com.example.agricultural_product.service;

import com.example.agricultural_product.pojo.AutoReplyRule;

import java.util.List;

public interface AutoReplyService {
    AutoReplyRule saveOrUpdate(AutoReplyRule rule, long sellerId);
    boolean deleteRule(long sellerId, long ruleId);
    boolean toggleRule(long sellerId, long ruleId, boolean enabled);
    List<AutoReplyRule> listMyRules(long sellerId);

    // 在买家发消息给卖家后尝试自动回复（只对接收方的规则生效）
    void tryAutoReplyForReceiver(long receiverId, long sessionId, long senderId, String incomingContent);
}