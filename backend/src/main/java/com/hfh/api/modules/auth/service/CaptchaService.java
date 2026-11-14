package com.hfh.api.modules.auth.service;

import com.hfh.api.config.SysConfigKeys;
import com.hfh.api.exception.BusinessException;
import com.hfh.api.modules.auth.dto.EmailCodeDTO;
import com.hfh.api.modules.auth.dto.SliderVerifyDTO;
import com.hfh.api.modules.auth.vo.SliderCaptchaVO;
import com.hfh.api.modules.system.service.ConfigService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Base64;
import java.util.Random;
import java.util.UUID;

/**
 * 验证码服务
 * 处理滑块验证码生成/校验、邮箱验证码发送/校验
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CaptchaService {

    private final StringRedisTemplate redisTemplate;
    private final JavaMailSender mailSender;
    private final ConfigService configService;
    private final TemplateEngine templateEngine;

    private static final int WIDTH = 280;
    private static final int HEIGHT = 155;
    private static final int PIECE_SIZE = 50;
    private static final int TOLERANCE = 5;

    // 默认配置值（秒）
    private static final int DEFAULT_CAPTCHA_EXPIRE_SECONDS = 300;
    private static final int DEFAULT_EMAIL_CODE_EXPIRE_SECONDS = 300;
    private static final int DEFAULT_EMAIL_LIMIT_SECONDS = 60;

    /** 邮箱验证码最大校验失败次数，超过后验证码作废 */
    private static final int MAX_EMAIL_CODE_FAIL_COUNT = 5;

    private final Random random = new Random();

    /**
     * 从系统配置读取秒数，并转换为 Duration
     */
    private Duration getSecondsDuration(String configKey, int defaultSeconds) {
        String value = configService.getConfigValue(configKey, String.valueOf(defaultSeconds));
        int seconds;
        try {
            seconds = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            seconds = defaultSeconds;
        }
        if (seconds <= 0) {
            seconds = defaultSeconds;
        }
        return Duration.ofSeconds(seconds);
    }

    /**
     * 生成滑块验证码
     */
    public SliderCaptchaVO generateSliderCaptcha() {
        int targetX = 55 + random.nextInt(160);
        int targetY = 40 + random.nextInt(60);

        // 1. 创建原始背景图
        BufferedImage original = createBackgroundImage();

        // 2. 提取滑块拼图
        BufferedImage sliderImage = extractSliderPiece(original, targetX, targetY);

        // 3. 创建带缺口的背景图
        BufferedImage backgroundImage = createBackgroundWithHole(original, targetX, targetY);

        // 4. 生成token并存入Redis
        String token = UUID.randomUUID().toString().replace("-", "");
        Duration captchaTtl = getSecondsDuration(SysConfigKeys.CAPTCHA_SLIDER_EXPIRE, DEFAULT_CAPTCHA_EXPIRE_SECONDS);
        redisTemplate.opsForValue().set(SysConfigKeys.REDIS_CAPTCHA_SLIDER_PREFIX + token, String.valueOf(targetX), captchaTtl);

        SliderCaptchaVO vo = new SliderCaptchaVO();
        vo.setToken(token);
        vo.setBackgroundImage("data:image/png;base64," + imageToBase64(backgroundImage));
        vo.setSliderImage("data:image/png;base64," + imageToBase64(sliderImage));
        vo.setSliderY(targetY);
        return vo;
    }

    /**
     * 校验滑块位置
     *
     * @return 验证通过后的临时 token，失败抛异常
     */
    public String verifySlider(SliderVerifyDTO dto) {
        String redisKey = SysConfigKeys.REDIS_CAPTCHA_SLIDER_PREFIX + dto.getToken();
        String targetXStr = redisTemplate.opsForValue().get(redisKey);
        if (targetXStr == null) {
            throw new BusinessException("验证已过期，请重新获取");
        }

        int targetX = Integer.parseInt(targetXStr);
        // 验证后删除，防止重复使用
        redisTemplate.delete(redisKey);

        if (Math.abs(dto.getPosition() - targetX) > TOLERANCE) {
            throw new BusinessException("滑块验证失败，请重试");
        }

        // 生成滑块验证通过的token
        String verifiedToken = UUID.randomUUID().toString().replace("-", "");
        Duration captchaTtl = getSecondsDuration(SysConfigKeys.CAPTCHA_SLIDER_EXPIRE, DEFAULT_CAPTCHA_EXPIRE_SECONDS);
        redisTemplate.opsForValue().set(SysConfigKeys.REDIS_CAPTCHA_VERIFIED_PREFIX + verifiedToken, "1", captchaTtl);
        return verifiedToken;
    }

    /**
     * 发送邮箱验证码
     */
    public void sendEmailCode(EmailCodeDTO dto) {
        // 1. 校验滑块验证token
        String verifiedKey = SysConfigKeys.REDIS_CAPTCHA_VERIFIED_PREFIX + dto.getCaptchaToken();
        String verified = redisTemplate.opsForValue().get(verifiedKey);
        if (verified == null) {
            throw new BusinessException("滑块验证已过期，请重新验证");
        }
        // 使用后删除，防止重复使用
        redisTemplate.delete(verifiedKey);

        // 2. 频率限制
        Duration limitTtl = getSecondsDuration(SysConfigKeys.EMAIL_SEND_LIMIT, DEFAULT_EMAIL_LIMIT_SECONDS);
        String limitKey = SysConfigKeys.REDIS_CAPTCHA_EMAIL_LIMIT_PREFIX + dto.getEmail();
        if (Boolean.TRUE.equals(redisTemplate.hasKey(limitKey))) {
            throw new BusinessException("发送过于频繁，请稍后再试");
        }

        // 3. 生成6位验证码
        String code = String.format("%06d", random.nextInt(1000000));

        // 4. 存入Redis
        Duration emailCodeTtl = getSecondsDuration(SysConfigKeys.EMAIL_VERIFY_EXPIRE, DEFAULT_EMAIL_CODE_EXPIRE_SECONDS);
        redisTemplate.opsForValue().set(SysConfigKeys.REDIS_CAPTCHA_EMAIL_PREFIX + dto.getEmail(), code, emailCodeTtl);

        // 5. 设置发送频率限制
        redisTemplate.opsForValue().set(limitKey, "1", limitTtl);

        // 6. 发送邮件
        try {
            long expireMinutes = Math.max(1, emailCodeTtl.toMinutes());
            sendVerificationEmail(dto.getEmail(), code, expireMinutes);
        } catch (Exception e) {
            log.error("发送邮箱验证码失败: email={}, error={}", dto.getEmail(), e.getMessage());
            // 发送失败时清除验证码和限制
            redisTemplate.delete(SysConfigKeys.REDIS_CAPTCHA_EMAIL_PREFIX + dto.getEmail());
            redisTemplate.delete(limitKey);
            throw new BusinessException("验证码发送失败，请稍后重试");
        }
    }

    /**
     * 校验邮箱验证码（注册时调用）
     * 校验失败会累计次数，超过 5 次后验证码作废，防止 6 位数字被暴力枚举
     */
    public boolean verifyEmailCode(String email, String code) {
        if (email == null || code == null) {
            return false;
        }
        String redisKey = SysConfigKeys.REDIS_CAPTCHA_EMAIL_PREFIX + email;
        String storedCode = redisTemplate.opsForValue().get(redisKey);
        if (storedCode == null) {
            return false;
        }
        if (storedCode.equals(code)) {
            // 验证成功后删除，防止重复使用
            redisTemplate.delete(redisKey);
            redisTemplate.delete(SysConfigKeys.REDIS_CAPTCHA_EMAIL_FAIL_PREFIX + email);
            return true;
        }

        // 校验失败计数，超过阈值后直接作废验证码
        String failKey = SysConfigKeys.REDIS_CAPTCHA_EMAIL_FAIL_PREFIX + email;
        Long failCount = redisTemplate.opsForValue().increment(failKey);
        int attempts = failCount == null ? 1 : failCount.intValue();
        if (attempts >= MAX_EMAIL_CODE_FAIL_COUNT) {
            redisTemplate.delete(redisKey);
            redisTemplate.delete(failKey);
            return false;
        }
        Duration failTtl = getSecondsDuration(SysConfigKeys.EMAIL_VERIFY_EXPIRE, DEFAULT_EMAIL_CODE_EXPIRE_SECONDS);
        redisTemplate.expire(failKey, failTtl);
        return false;
    }

    /**
     * 创建背景图片（随机渐变+几何形状）
     */
    private BufferedImage createBackgroundImage() {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 随机渐变背景
        Color color1 = new Color(180 + random.nextInt(76), 180 + random.nextInt(76), 180 + random.nextInt(76));
        Color color2 = new Color(180 + random.nextInt(76), 180 + random.nextInt(76), 180 + random.nextInt(76));
        GradientPaint gradient = new GradientPaint(0, 0, color1, WIDTH, HEIGHT, color2);
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        // 添加随机几何形状
        for (int i = 0; i < 20; i++) {
            int r = random.nextInt(256);
            int g = random.nextInt(256);
            int b = random.nextInt(256);
            int alpha = 40 + random.nextInt(120);
            g2d.setColor(new Color(r, g, b, alpha));
            int shapeType = random.nextInt(3);
            switch (shapeType) {
                case 0 -> g2d.fillOval(random.nextInt(WIDTH), random.nextInt(HEIGHT),
                        20 + random.nextInt(60), 20 + random.nextInt(60));
                case 1 -> g2d.fillRect(random.nextInt(WIDTH), random.nextInt(HEIGHT),
                        20 + random.nextInt(60), 20 + random.nextInt(60));
                case 2 -> {
                    g2d.setStroke(new BasicStroke(2 + random.nextInt(5)));
                    g2d.drawLine(random.nextInt(WIDTH), random.nextInt(HEIGHT),
                            random.nextInt(WIDTH), random.nextInt(HEIGHT));
                }
            }
        }
        g2d.dispose();
        return image;
    }

    /**
     * 提取滑块拼图图片
     */
    private BufferedImage extractSliderPiece(BufferedImage original, int x, int y) {
        BufferedImage piece = new BufferedImage(PIECE_SIZE, PIECE_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = piece.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 圆角矩形裁剪
        RoundRectangle2D roundRect = new RoundRectangle2D.Float(0, 0, PIECE_SIZE, PIECE_SIZE, 12, 12);
        g2d.setClip(roundRect);
        g2d.drawImage(original, 0, 0, PIECE_SIZE, PIECE_SIZE,
                x, y, x + PIECE_SIZE, y + PIECE_SIZE, null);

        // 边框
        g2d.setClip(null);
        g2d.setColor(new Color(255, 255, 255, 200));
        g2d.setStroke(new BasicStroke(2.5f));
        g2d.draw(roundRect);

        // 阴影效果
        g2d.setColor(new Color(0, 0, 0, 40));
        g2d.setStroke(new BasicStroke(4f));
        g2d.draw(roundRect);

        g2d.dispose();
        return piece;
    }

    /**
     * 创建带缺口的背景图
     */
    private BufferedImage createBackgroundWithHole(BufferedImage original, int x, int y) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 绘制原始背景
        g2d.drawImage(original, 0, 0, null);

        // 绘制缺口（半透明黑色遮罩）
        RoundRectangle2D hole = new RoundRectangle2D.Float(x, y, PIECE_SIZE, PIECE_SIZE, 12, 12);
        g2d.setColor(new Color(0, 0, 0, 100));
        g2d.fill(hole);

        // 缺口边框
        g2d.setColor(new Color(255, 255, 255, 180));
        g2d.setStroke(new BasicStroke(2f));
        g2d.draw(hole);

        g2d.dispose();
        return image;
    }

    /**
     * 图片转base64
     */
    private String imageToBase64(BufferedImage image) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("图片转换失败", e);
        }
    }

    /**
     * 发送验证码邮件
     */
    private void sendVerificationEmail(String email, String code, long expireMinutes) throws MessagingException, java.io.UnsupportedEncodingException {
        // 渲染邮件模板（templates/mail/verification-code.html）
        Context context = new Context();
        context.setVariable("code", code);
        context.setVariable("expireMinutes", expireMinutes);
        String htmlContent = templateEngine.process("mail/verification-code", context);

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
        String from = configService.getConfigValue(SysConfigKeys.EMAIL_SENDER, "");
        if (from.isEmpty()) {
            throw new BusinessException("未配置发件人邮箱 email.sender");
        }
        helper.setFrom(from, "Single Admin");
        helper.setTo(email);
        helper.setSubject("Single Admin - 邮箱验证码");
        helper.setText(htmlContent, true);
        mailSender.send(mimeMessage);
    }
}
