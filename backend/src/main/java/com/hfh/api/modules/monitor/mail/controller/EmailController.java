package com.hfh.api.modules.monitor.mail.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.util.SaResult;
import com.hfh.api.base.BaseController;
import com.hfh.api.modules.monitor.mail.dto.EmailRequest;
import com.hfh.api.modules.monitor.mail.dto.Recipient;
import com.hfh.api.modules.monitor.mail.dto.TemplateEmailRequest;
import com.hfh.api.modules.monitor.mail.service.EmailService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 邮件发送接口
 * <p>提供简单文本、HTML（含附件）、Thymeleaf 模板、批量群发四种发送方式，均异步执行</p>
 */
@Slf4j
@RestController(value = "emailController")
@RequestMapping("/admin/email")
@RequiredArgsConstructor
@Validated
@Tag(name = "邮件发送", description = "邮件发送接口")
public class EmailController extends BaseController {

    private final EmailService emailService;

    /**
     * 发送简单文本邮件
     */
    @SaCheckPermission("email:send")
    @PostMapping("/simple")
    @Operation(summary = "发送简单文本邮件")
    public SaResult sendSimpleEmail(@Valid @RequestBody EmailRequest request) {
        CompletableFuture<Void> future = emailService.sendSimpleEmailAsync(request);
        SaResult result = SaResult.ok("简单邮件已加入异步发送队列");
        result.set("recipients", getRecipients(request));
        return result;
    }

    /**
     * 发送HTML邮件（支持群发 + 附件）
     */
    @SaCheckPermission("email:send")
    @PostMapping("/html")
    @Operation(summary = "发送HTML邮件")
    public SaResult sendHtmlEmail(@Valid @RequestBody EmailRequest request) {
        request.setHtml(true);
        CompletableFuture<Void> future = emailService.sendHtmlEmailAsync(request);
        SaResult result = SaResult.ok("HTML邮件已加入异步发送队列");
        result.set("recipients", getRecipients(request));
        return result;
    }

    /**
     * 发送Thymeleaf模板邮件
     */
    @SaCheckPermission("email:send")
    @PostMapping("/template")
    @Operation(summary = "发送Thymeleaf模板邮件")
    public SaResult sendTemplateEmail(@Valid @RequestBody TemplateEmailRequest request) {
        CompletableFuture<Void> future = emailService.sendTemplateEmailAsync(request);
        SaResult result = SaResult.ok("模板邮件已加入异步发送队列");
        result.set("recipients", getRecipients(request));
        return result;
    }

    /**
     * 批量群发
     */
    @SaCheckPermission("email:batch")
    @PostMapping("/batch")
    @Operation(summary = "批量群发邮件")
    public SaResult sendBatchEmails(@Valid @RequestBody List<EmailRequest> requests) {
        CompletableFuture<Void> future = emailService.sendBatchEmailsAsync(requests);
        SaResult result = SaResult.ok("批量邮件已加入异步发送队列");
        result.set("count", requests.size());
        return result;
    }

    /**
     * 获取收件人展示列表
     */
    private List<String> getRecipients(Recipient request) {
        return request.getToList() != null ? request.getToList() : List.of(request.getTo());
    }
}
