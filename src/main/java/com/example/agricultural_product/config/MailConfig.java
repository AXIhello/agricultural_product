package com.example.agricultural_product.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.qq.com");
        mailSender.setPort(465);
        mailSender.setUsername("3410557089@qq.com"); // 发件人邮箱
        mailSender.setPassword("ysgjttwcukkgchhb");     // SMTP授权码

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtps");
        props.put("mail.smtps.auth", "true");
        props.put("mail.smtps.ssl.enable", "true");
        props.put("mail.debug", "true"); // 开启调试

        return mailSender;
    }
}
