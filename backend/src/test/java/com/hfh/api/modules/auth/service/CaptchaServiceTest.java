package com.hfh.api.modules.auth.service;

import com.hfh.api.config.SysConfigKeys;
import com.hfh.api.exception.BusinessException;
import com.hfh.api.modules.auth.dto.EmailCodeDTO;
import com.hfh.api.modules.auth.dto.SliderVerifyDTO;
import com.hfh.api.modules.auth.vo.SliderCaptchaVO;
import com.hfh.api.modules.system.service.ConfigService;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;

import java.time.Duration;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CaptchaServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @Mock
    private JavaMailSender mailSender;
    @Mock
    private ConfigService configService;
    @Mock
    private TemplateEngine templateEngine;

    private CaptchaService captchaService;

    @BeforeEach
    void setUp() {
        captchaService = new CaptchaService(redisTemplate, mailSender, configService, templateEngine);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void generateSliderCaptchaShouldStoreTargetAndReturnImages() {
        when(configService.getConfigValue(eq(SysConfigKeys.CAPTCHA_SLIDER_EXPIRE), anyString())).thenReturn("300");

        SliderCaptchaVO vo = captchaService.generateSliderCaptcha();

        assertThat(vo.getToken()).hasSize(32);
        assertThat(vo.getBackgroundImage()).startsWith("data:image/png;base64,");
        assertThat(vo.getSliderImage()).startsWith("data:image/png;base64,");
        assertThat(vo.getSliderY()).isBetween(40, 99);
        verify(valueOperations).set(eq(SysConfigKeys.REDIS_CAPTCHA_SLIDER_PREFIX + vo.getToken()),
                anyString(), eq(Duration.ofSeconds(300)));
    }

    @Test
    void generateSliderCaptchaShouldFallbackToDefaultWhenConfigInvalid() {
        when(configService.getConfigValue(eq(SysConfigKeys.CAPTCHA_SLIDER_EXPIRE), anyString())).thenReturn("abc");

        SliderCaptchaVO vo = captchaService.generateSliderCaptcha();

        verify(valueOperations).set(anyString(), anyString(), eq(Duration.ofSeconds(300)));
        assertThat(vo.getToken()).isNotBlank();
    }

    @Test
    void generateSliderCaptchaShouldFallbackWhenConfigNotPositive() {
        when(configService.getConfigValue(eq(SysConfigKeys.CAPTCHA_SLIDER_EXPIRE), anyString())).thenReturn("0");

        captchaService.generateSliderCaptcha();

        verify(valueOperations).set(anyString(), anyString(), eq(Duration.ofSeconds(300)));
    }

    @Test
    void verifySliderShouldRejectExpired() {
        SliderVerifyDTO dto = new SliderVerifyDTO();
        dto.setToken("t");
        dto.setPosition(20);
        when(valueOperations.get(SysConfigKeys.REDIS_CAPTCHA_SLIDER_PREFIX + "t")).thenReturn(null);

        assertThatThrownBy(() -> captchaService.verifySlider(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessage("验证已过期，请重新获取");
    }

    @Test
    void verifySliderShouldRejectWrongPosition() {
        SliderVerifyDTO dto = new SliderVerifyDTO();
        dto.setToken("t");
        dto.setPosition(10);
        when(valueOperations.get(SysConfigKeys.REDIS_CAPTCHA_SLIDER_PREFIX + "t")).thenReturn("100");

        assertThatThrownBy(() -> captchaService.verifySlider(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessage("滑块验证失败，请重试");
        verify(redisTemplate).delete(SysConfigKeys.REDIS_CAPTCHA_SLIDER_PREFIX + "t");
    }

    @Test
    void verifySliderShouldReturnVerifiedTokenWithinTolerance() {
        SliderVerifyDTO dto = new SliderVerifyDTO();
        dto.setToken("t");
        dto.setPosition(104);
        when(valueOperations.get(SysConfigKeys.REDIS_CAPTCHA_SLIDER_PREFIX + "t")).thenReturn("100");
        when(configService.getConfigValue(eq(SysConfigKeys.CAPTCHA_SLIDER_EXPIRE), anyString())).thenReturn("300");

        String verified = captchaService.verifySlider(dto);

        assertThat(verified).hasSize(32);
        verify(valueOperations).set(SysConfigKeys.REDIS_CAPTCHA_VERIFIED_PREFIX + verified, "1",
                Duration.ofSeconds(300));
    }

    @Test
    void sendEmailCodeShouldRequireSliderVerification() {
        EmailCodeDTO dto = new EmailCodeDTO();
        dto.setEmail("a@b.com");
        dto.setCaptchaToken("c");
        when(valueOperations.get(SysConfigKeys.REDIS_CAPTCHA_VERIFIED_PREFIX + "c")).thenReturn(null);

        assertThatThrownBy(() -> captchaService.sendEmailCode(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessage("滑块验证已过期，请重新验证");
    }

    @Test
    void sendEmailCodeShouldRejectTooFrequent() {
        EmailCodeDTO dto = new EmailCodeDTO();
        dto.setEmail("a@b.com");
        dto.setCaptchaToken("c");
        when(valueOperations.get(SysConfigKeys.REDIS_CAPTCHA_VERIFIED_PREFIX + "c")).thenReturn("1");
        when(redisTemplate.hasKey(SysConfigKeys.REDIS_CAPTCHA_EMAIL_LIMIT_PREFIX + "a@b.com")).thenReturn(true);

        assertThatThrownBy(() -> captchaService.sendEmailCode(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessage("发送过于频繁，请稍后再试");
    }

    @Test
    void sendEmailCodeShouldStoreCodeAndSendMail() {
        EmailCodeDTO dto = new EmailCodeDTO();
        dto.setEmail("a@b.com");
        dto.setCaptchaToken("c");
        when(valueOperations.get(SysConfigKeys.REDIS_CAPTCHA_VERIFIED_PREFIX + "c")).thenReturn("1");
        when(redisTemplate.hasKey(SysConfigKeys.REDIS_CAPTCHA_EMAIL_LIMIT_PREFIX + "a@b.com")).thenReturn(false);
        when(configService.getConfigValue(eq(SysConfigKeys.EMAIL_SEND_LIMIT), anyString())).thenReturn("60");
        when(configService.getConfigValue(eq(SysConfigKeys.EMAIL_VERIFY_EXPIRE), anyString())).thenReturn("300");
        when(configService.getConfigValue(eq(SysConfigKeys.EMAIL_SENDER), anyString())).thenReturn("no-reply@example.com");
        when(templateEngine.process(eq("mail/verification-code"), any())).thenReturn("<html>code</html>");
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage(Session.getInstance(new Properties())));

        captchaService.sendEmailCode(dto);

        verify(redisTemplate).delete(SysConfigKeys.REDIS_CAPTCHA_VERIFIED_PREFIX + "c");
        verify(valueOperations).set(eq(SysConfigKeys.REDIS_CAPTCHA_EMAIL_PREFIX + "a@b.com"),
                anyString(), eq(Duration.ofSeconds(300)));
        verify(valueOperations).set(SysConfigKeys.REDIS_CAPTCHA_EMAIL_LIMIT_PREFIX + "a@b.com",
                "1", Duration.ofSeconds(60));
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendEmailCodeShouldCleanupWhenMailFails() {
        EmailCodeDTO dto = new EmailCodeDTO();
        dto.setEmail("a@b.com");
        dto.setCaptchaToken("c");
        when(valueOperations.get(SysConfigKeys.REDIS_CAPTCHA_VERIFIED_PREFIX + "c")).thenReturn("1");
        when(configService.getConfigValue(eq(SysConfigKeys.EMAIL_SENDER), anyString())).thenReturn("");
        when(templateEngine.process(eq("mail/verification-code"), any())).thenReturn("html");
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage(Session.getInstance(new Properties())));

        assertThatThrownBy(() -> captchaService.sendEmailCode(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessage("验证码发送失败，请稍后重试");

        verify(redisTemplate).delete(SysConfigKeys.REDIS_CAPTCHA_EMAIL_PREFIX + "a@b.com");
        verify(redisTemplate).delete(SysConfigKeys.REDIS_CAPTCHA_EMAIL_LIMIT_PREFIX + "a@b.com");
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void verifyEmailCodeShouldHandleNullAndMissing() {
        assertThat(captchaService.verifyEmailCode(null, "1")).isFalse();
        assertThat(captchaService.verifyEmailCode("a@b.com", null)).isFalse();
        when(valueOperations.get("captcha:email:a@b.com")).thenReturn(null);
        assertThat(captchaService.verifyEmailCode("a@b.com", "1")).isFalse();
    }

    @Test
    void verifyEmailCodeShouldCheckCodeAndDeleteOnSuccess() {
        when(valueOperations.get("captcha:email:a@b.com")).thenReturn("123456");

        assertThat(captchaService.verifyEmailCode("a@b.com", "000000")).isFalse();
        assertThat(captchaService.verifyEmailCode("a@b.com", "123456")).isTrue();
        verify(redisTemplate).delete("captcha:email:a@b.com");
    }
}
