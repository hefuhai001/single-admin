package com.hfh.api.modules.monitor.mail.service;

import com.hfh.api.config.SysConfigKeys;
import com.hfh.api.modules.monitor.mail.dto.EmailRequest;
import com.hfh.api.modules.monitor.mail.dto.TemplateEmailRequest;
import com.hfh.api.modules.monitor.mail.dto.WelcomeTemplateVariables;
import com.hfh.api.modules.system.service.ConfigService;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;
    @Mock
    private TemplateEngine templateEngine;
    @Mock
    private ConfigService configService;

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailService(mailSender, templateEngine, configService);
        when(configService.getConfigValue(SysConfigKeys.EMAIL_SENDER, "")).thenReturn("no-reply@example.com");
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage(Session.getInstance(new Properties())));
    }

    private EmailRequest request() {
        EmailRequest request = new EmailRequest();
        request.setTo("a@b.com");
        request.setSubject("subject");
        request.setContent("content");
        return request;
    }

    @Test
    void sendSimpleEmailShouldSupportSingleRecipient() {
        CompletableFuture<Void> future = emailService.sendSimpleEmailAsync(request());

        assertThat(future).isCompleted();
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendSimpleEmailShouldSupportRecipientListAndCc() {
        EmailRequest request = request();
        request.setToList(List.of("a@b.com", "c@d.com"));
        request.setCc(List.of("e@f.com"));

        assertThat(emailService.sendSimpleEmailAsync(request)).isCompleted();
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendSimpleEmailShouldFailWhenSenderMissing() {
        when(configService.getConfigValue(SysConfigKeys.EMAIL_SENDER, "")).thenReturn("");

        CompletableFuture<Void> future = emailService.sendSimpleEmailAsync(request());

        assertThat(future).isCompletedExceptionally();
    }

    @Test
    void sendHtmlEmailShouldAttachExistingFile(@TempDir Path tempDir) throws Exception {
        Path file = Files.writeString(tempDir.resolve("report.txt"), "data");
        EmailRequest request = request();
        request.setContent("<h1>hi</h1>");
        request.setToList(List.of("a@b.com"));
        request.setCc(List.of("c@d.com"));
        request.setBcc(List.of("e@f.com"));
        request.setAttachments(List.of(file.toString(), tempDir.resolve("missing.txt").toString()));

        assertThat(emailService.sendHtmlEmailAsync(request)).isCompleted();
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendHtmlEmailShouldFailWhenMailSenderThrows() {
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("smtp down"));

        assertThat(emailService.sendHtmlEmailAsync(request())).isCompletedExceptionally();
    }

    @Test
    void sendTemplateEmailShouldRenderVariables() {
        TemplateEmailRequest request = new TemplateEmailRequest();
        request.setTo("a@b.com");
        request.setSubject("welcome");
        request.setTemplateName("mail/welcome");
        WelcomeTemplateVariables vars = new WelcomeTemplateVariables();
        vars.setUsername("alice");
        vars.setCode("123456");
        vars.setLink("http://x");
        vars.setExpireMinutes(30);
        request.setVariables(vars);
        when(templateEngine.process(eq("mail/welcome"), any(Context.class))).thenReturn("<p>hi</p>");

        assertThat(emailService.sendTemplateEmailAsync(request)).isCompleted();
        verify(templateEngine).process(eq("mail/welcome"), any(Context.class));
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendTemplateEmailShouldHandleNullVariablesAndRecipientList() {
        TemplateEmailRequest request = new TemplateEmailRequest();
        request.setToList(List.of("a@b.com"));
        request.setSubject("s");
        request.setTemplateName("mail/welcome");
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("html");

        assertThat(emailService.sendTemplateEmailAsync(request)).isCompleted();
    }

    @Test
    void sendTemplateEmailShouldFailWhenTemplateThrows() {
        TemplateEmailRequest request = new TemplateEmailRequest();
        request.setTo("a@b.com");
        request.setTemplateName("missing");
        when(templateEngine.process(anyString(), any(Context.class))).thenThrow(new RuntimeException("tpl"));

        assertThat(emailService.sendTemplateEmailAsync(request)).isCompletedExceptionally();
    }

    @Test
    void sendBatchShouldDispatchByHtmlFlag() {
        EmailRequest simple = request();
        EmailRequest html = request();
        html.setHtml(true);

        CompletableFuture<Void> future = emailService.sendBatchEmailsAsync(List.of(simple, html));

        assertThat(future).isCompleted();
        verify(mailSender).send(any(SimpleMailMessage.class));
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendBatchShouldPropagateFailure() {
        when(configService.getConfigValue(SysConfigKeys.EMAIL_SENDER, "")).thenReturn("");
        EmailRequest simple = request();

        assertThat(emailService.sendBatchEmailsAsync(List.of(simple))).isCompletedExceptionally();
    }

    @Test
    void sendSimpleEmailShouldFailWhenSenderReturnsNull() {
        ConfigService mockConfig = mock(ConfigService.class);
        when(mockConfig.getConfigValue(SysConfigKeys.EMAIL_SENDER, "")).thenReturn(null);
        EmailService service = new EmailService(mailSender, templateEngine, mockConfig);

        assertThat(service.sendSimpleEmailAsync(request())).isCompletedExceptionally();
    }
}
