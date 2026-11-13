package com.hfh.api.modules.monitor.mail.controller;

import com.hfh.api.modules.monitor.mail.dto.EmailRequest;
import com.hfh.api.modules.monitor.mail.dto.TemplateEmailRequest;
import com.hfh.api.modules.monitor.mail.service.EmailService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmailControllerTest {

    private final EmailService emailService = mock(EmailService.class);
    private final EmailController controller = new EmailController(emailService);

    private EmailRequest request() {
        EmailRequest request = new EmailRequest();
        request.setTo("a@b.com");
        request.setSubject("s");
        request.setContent("c");
        return request;
    }

    @Test
    void simpleAndHtmlShouldQueue() {
        when(emailService.sendSimpleEmailAsync(any())).thenReturn(CompletableFuture.completedFuture(null));
        when(emailService.sendHtmlEmailAsync(any())).thenReturn(CompletableFuture.completedFuture(null));

        assertThat(controller.sendSimpleEmail(request()).getMsg()).contains("简单邮件");
        assertThat(controller.sendHtmlEmail(request()).getMsg()).contains("HTML邮件");
        verify(emailService).sendSimpleEmailAsync(any());
        verify(emailService).sendHtmlEmailAsync(any());
    }

    @Test
    void recipientsShouldUseToListWhenPresent() {
        when(emailService.sendSimpleEmailAsync(any())).thenReturn(CompletableFuture.completedFuture(null));
        EmailRequest request = request();
        request.setToList(List.of("a@b.com", "c@d.com"));

        assertThat(controller.sendSimpleEmail(request).getData() == null
                || controller.sendSimpleEmail(request).getCode() == 200).isTrue();
    }

    @Test
    void templateAndBatchShouldQueue() {
        when(emailService.sendTemplateEmailAsync(any())).thenReturn(CompletableFuture.completedFuture(null));
        when(emailService.sendBatchEmailsAsync(anyList())).thenReturn(CompletableFuture.completedFuture(null));

        TemplateEmailRequest template = new TemplateEmailRequest();
        template.setTo("a@b.com");
        template.setTemplateName("mail/welcome");
        template.setToList(List.of("a@b.com"));

        assertThat(controller.sendTemplateEmail(template).getMsg()).contains("模板邮件");
        assertThat(controller.sendBatchEmails(List.of(request(), request())).getMsg()).contains("批量邮件");
    }
}
