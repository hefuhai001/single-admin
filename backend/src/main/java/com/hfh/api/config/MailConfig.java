package com.hfh.api.config;

import com.hfh.api.modules.system.service.ConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * 邮箱配置
 * <p>
 * 从系统配置表读取邮箱服务器、账号、授权码，手动创建 JavaMailSender Bean，
 * 避免将邮箱敏感信息硬编码在 application-*.yaml 中。
 * </p>
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class MailConfig {

    private final ConfigService configService;

    /**
     * 创建 JavaMailSender Bean
     */
    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();

        String host = configService.getConfigValue("email.smtp.host", "smtp.163.com");
        int port = parseInt(configService.getConfigValue("email.smtp.port", "465"), 465);
        String username = configService.getConfigValue("email.sender", "18365404517@163.com");
        String password = configService.getConfigValue("email.sender.password", "NPRDDKVXCLILSNQP");

        sender.setHost(host);
        sender.setPort(port);
        sender.setUsername(username);
        sender.setPassword(password);
        sender.setDefaultEncoding("UTF-8");

        // 163 邮箱 465 端口使用 SSL 直连，这里显式启用 SSL SocketFactory
        Properties properties = new Properties();
        properties.setProperty("mail.transport.protocol", "smtp");
        properties.setProperty("mail.smtp.auth", "true");
        properties.setProperty("mail.smtp.ssl.enable", "true");
        properties.setProperty("mail.smtp.starttls.enable", "false");
        properties.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.setProperty("mail.smtp.socketFactory.fallback", "false");
        properties.setProperty("mail.smtp.socketFactory.port", String.valueOf(port));
        properties.setProperty("mail.smtp.timeout", "10000");
        properties.setProperty("mail.smtp.connectiontimeout", "10000");
        sender.setJavaMailProperties(properties);

        log.info("JavaMailSender 已初始化: host={}, port={}, username={}", host, port, username);
        return sender;
    }

    private int parseInt(String value, int defaultValue) {
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
