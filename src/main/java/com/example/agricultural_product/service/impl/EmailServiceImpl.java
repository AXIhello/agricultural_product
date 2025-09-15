package com.example.agricultural_product.service.impl;

import com.example.agricultural_product.service.EmailService;
import jakarta.annotation.Resource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class EmailServiceImpl implements EmailService {

    @Resource
    private JavaMailSender mailSender;

    // 存储验证码
    private final Map<String, String> verificationCodes = new ConcurrentHashMap<>();

    // 验证码有效期（毫秒）
    private static final long CODE_EXPIRY_TIME = 5 * 60 * 1000; // 5分钟

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Override
    public String sendVerificationCode(String to) {
        String verificationCode = generateVerificationCode();

        // 创建邮件
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("3410557089@qq.com"); // 发件人邮箱
        message.setTo(to);
        message.setSubject("验证码");
        message.setText("您的验证码是：" + verificationCode + "，有效期为5分钟，请勿泄露给他人。");

        // 发送邮件
        mailSender.send(message);

        // 保存验证码
        verificationCodes.put(to, verificationCode);

        // 定时删除过期验证码
        scheduler.schedule(() -> verificationCodes.remove(to), CODE_EXPIRY_TIME, TimeUnit.MILLISECONDS);

        return verificationCode;
    }

    @Override
    public boolean verifyCode(String email, String code) {
        String savedCode = verificationCodes.get(email);
        return savedCode != null && savedCode.equals(code);
    }

    // 生成6位随机验证码
    private String generateVerificationCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
}
