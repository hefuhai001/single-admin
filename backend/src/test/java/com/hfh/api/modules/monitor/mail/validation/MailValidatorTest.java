package com.hfh.api.modules.monitor.mail.validation;

import com.hfh.api.modules.monitor.mail.dto.EmailRequest;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MailValidatorTest {

    private final AtLeastOneRecipientValidator recipientValidator = new AtLeastOneRecipientValidator();
    private final ValidEmailListValidator emailListValidator = new ValidEmailListValidator();
    private final ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

    @Test
    void atLeastOneRecipientShouldValidate() {
        assertThat(recipientValidator.isValid(null, context)).isFalse();

        EmailRequest request = new EmailRequest();
        assertThat(recipientValidator.isValid(request, context)).isFalse();

        request.setTo("   ");
        assertThat(recipientValidator.isValid(request, context)).isFalse();

        request.setTo("a@b.com");
        assertThat(recipientValidator.isValid(request, context)).isTrue();

        request.setTo("  ");
        request.setToList(List.of("a@b.com"));
        assertThat(recipientValidator.isValid(request, context)).isTrue();
    }

    @Test
    void emailListShouldAcceptNullOrEmpty() {
        assertThat(emailListValidator.isValid(null, context)).isTrue();
        assertThat(emailListValidator.isValid(List.of(), context)).isTrue();
    }

    @Test
    void emailListShouldAcceptValidAddresses() {
        assertThat(emailListValidator.isValid(List.of("a@b.com", "x.y+z@sub.domain.cn"), context)).isTrue();
        verify(context, never()).disableDefaultConstraintViolation();
    }

    @Test
    void emailListShouldRejectInvalidAddresses() {
        ConstraintValidatorContext.ConstraintViolationBuilder builder =
                mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);

        assertThat(emailListValidator.isValid(java.util.Arrays.asList("a@b.com", "bad", null), context)).isFalse();

        verify(context, org.mockito.Mockito.times(2)).disableDefaultConstraintViolation();
        verify(builder, org.mockito.Mockito.times(2)).addConstraintViolation();
    }
}
