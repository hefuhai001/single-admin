package com.hfh.api.modules.monitor.mail.service;

import com.hfh.api.config.SysConfigKeys;
import com.hfh.api.modules.monitor.mail.dto.EmailRequest;
import com.hfh.api.modules.monitor.mail.dto.TemplateEmailRequest;
import com.hfh.api.modules.monitor.mail.dto.WelcomeTemplateVariables;
import com.hfh.api.modules.system.service.ConfigService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 邮件发送服务
 * <p>
 * 支持简单文本、HTML（含附件）、Thymeleaf 模板、批量群发四种发送方式，
 * 所有发送均在 {@code mailTaskExecutor} 线程池中异步执行，不阻塞请求线程。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    /** 发件人默认值（与 sys_config 中 email.sender 一致） */
    private static final String DEFAULT_FROM = "18365404517@163.com";

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final ConfigService configService;

    /**
     * 获取发件人邮箱（从系统配置表读取，未配置时使用默认值）
     */
    private String getFrom() {
        return configService.getConfigValue(SysConfigKeys.EMAIL_SENDER, DEFAULT_FROM);
    }

    /**
     * 异步发送简单文本邮件（支持群发）
     */
    @Async("mailTaskExecutor")
    public CompletableFuture<Void> sendSimpleEmailAsync(EmailRequest request) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(getFrom());

            // 支持多收件人
            if (request.getToList() != null && !request.getToList().isEmpty()) {
                message.setTo(request.getToList().toArray(new String[0]));
            } else {
                message.setTo(request.getTo());
            }

            message.setSubject(request.getSubject());
            message.setText(request.getContent());

            if (request.getCc() != null && !request.getCc().isEmpty()) {
                message.setCc(request.getCc().toArray(new String[0]));
            }

            mailSender.send(message);
            log.info("✅ 简单邮件已异步发送至: {}", request.getToList() != null ? request.getToList() : request.getTo());
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("❌ 简单邮件发送失败: {}", e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * 异步发送HTML邮件（支持群发 + 附件）
     */
    @Async("mailTaskExecutor")
    public CompletableFuture<Void> sendHtmlEmailAsync(EmailRequest request) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(getFrom());

            // 支持多收件人
            if (request.getToList() != null && !request.getToList().isEmpty()) {
                helper.setTo(request.getToList().toArray(new String[0]));
            } else {
                helper.setTo(request.getTo());
            }

            helper.setSubject(request.getSubject());
            helper.setText(request.getContent(), true);

            if (request.getCc() != null && !request.getCc().isEmpty()) {
                helper.setCc(request.getCc().toArray(new String[0]));
            }
            if (request.getBcc() != null && !request.getBcc().isEmpty()) {
                helper.setBcc(request.getBcc().toArray(new String[0]));
            }

            // 添加附件
            if (request.getAttachments() != null) {
                for (String filePath : request.getAttachments()) {
                    File file = new File(filePath);
                    if (file.exists()) {
                        FileSystemResource resource = new FileSystemResource(file);
                        helper.addAttachment(file.getName(), resource);
                        log.info("📎 已添加附件: {}", file.getName());
                    } else {
                        log.warn("⚠️ 附件不存在: {}", filePath);
                    }
                }
            }

            mailSender.send(mimeMessage);
            log.info("✅ HTML邮件已异步发送至: {}", request.getToList() != null ? request.getToList() : request.getTo());
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("❌ HTML邮件发送失败: {}", e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * 异步发送模板邮件（支持群发）
     */
    @Async("mailTaskExecutor")
    public CompletableFuture<Void> sendTemplateEmailAsync(TemplateEmailRequest request) {
        try {
            Context context = new Context();

            // 将具体类型的变量对象转换为 Thymeleaf 上下文变量
            WelcomeTemplateVariables vars = request.getVariables();
            if (vars != null) {
                context.setVariable("username", vars.getUsername());
                context.setVariable("code", vars.getCode());
                context.setVariable("link", vars.getLink());
                context.setVariable("expireMinutes", vars.getExpireMinutes());
            }

            String htmlContent = templateEngine.process(request.getTemplateName(), context);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(getFrom());

            if (request.getToList() != null && !request.getToList().isEmpty()) {
                helper.setTo(request.getToList().toArray(new String[0]));
            } else {
                helper.setTo(request.getTo());
            }

            helper.setSubject(request.getSubject());
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            log.info("✅ 模板邮件已异步发送至: {}",
                    request.getToList() != null ? request.getToList() : request.getTo());
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("❌ 模板邮件发送失败: {}", e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * 批量群发（每个收件人独立异步发送，避免互相影响）
     */
    @Async("mailTaskExecutor")
    public CompletableFuture<Void> sendBatchEmailsAsync(List<EmailRequest> requests) {
        List<CompletableFuture<Void>> futures = requests.stream()
                .map(req -> req.isHtml() ? sendHtmlEmailAsync(req) : sendSimpleEmailAsync(req))
                .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }
}
