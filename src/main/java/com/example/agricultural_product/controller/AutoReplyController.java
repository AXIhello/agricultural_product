package com.example.agricultural_product.controller;

import com.example.agricultural_product.pojo.AutoReplyRule;
import com.example.agricultural_product.service.AutoReplyService;
import com.example.agricultural_product.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat/auto-replies")
public class AutoReplyController {

    private final AutoReplyService autoReplyService;

    public AutoReplyController(AutoReplyService autoReplyService) {
        this.autoReplyService = autoReplyService;
    }

    private long uid(HttpServletRequest req) {
        String auth = req.getHeader("Authorization");
        String token = (auth != null && auth.startsWith("Bearer ")) ? auth.substring(7) : req.getParameter("token");
        if (token == null || token.isEmpty()) throw new RuntimeException("未认证");
        return JwtUtil.getUserId(token);
    }

    @GetMapping
    public List<AutoReplyRule> myRules(HttpServletRequest req) {
        return autoReplyService.listMyRules(uid(req));
    }

    @PostMapping
    public AutoReplyRule save(@RequestBody AutoReplyRule rule, HttpServletRequest req) {
        return autoReplyService.saveOrUpdate(rule, uid(req));
    }

    @DeleteMapping("/{ruleId}")
    public Map<String, Object> delete(@PathVariable Long ruleId, HttpServletRequest req) {
        boolean ok = autoReplyService.deleteRule(uid(req), ruleId);
        return Map.of("success", ok);
    }

    @PutMapping("/{ruleId}/toggle")
    public Map<String, Object> toggle(@PathVariable Long ruleId, @RequestParam boolean enabled, HttpServletRequest req) {
        boolean ok = autoReplyService.toggleRule(uid(req), ruleId, enabled);
        return Map.of("success", ok);
    }
}