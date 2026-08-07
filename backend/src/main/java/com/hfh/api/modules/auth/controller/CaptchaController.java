package com.hfh.api.modules.auth.controller;

import com.hfh.api.modules.auth.dto.EmailCodeDTO;
import com.hfh.api.modules.auth.dto.SliderVerifyDTO;
import com.hfh.api.modules.auth.service.CaptchaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import cn.dev33.satoken.util.SaResult;

/**
 * 验证码控制器
 * 提供滑块验证码、邮箱验证码等接口
 */
@RestController
@RequestMapping("/captcha")
@RequiredArgsConstructor
@Tag(name = "验证码管理", description = "滑块验证码、邮箱验证码等接口")
public class CaptchaController {

    private final CaptchaService captchaService;

    /**
     * 获取滑块验证码
     */
    @GetMapping("/slider")
    @Operation(summary = "获取滑块验证码", description = "生成滑块拼图验证码图片")
    public SaResult getSliderCaptcha() {
        return SaResult.data(captchaService.generateSliderCaptcha());
    }

    /**
     * 校验滑块位置
     */
    @PostMapping("/slider/verify")
    @Operation(summary = "校验滑块位置", description = "校验用户拖动滑块的位置是否正确")
    public SaResult verifySlider(@Valid @RequestBody SliderVerifyDTO dto) {
        return SaResult.data(captchaService.verifySlider(dto));
    }

    /**
     * 发送邮箱验证码（需先通过滑块验证）
     */
    @PostMapping("/email/send")
    @Operation(summary = "发送邮箱验证码", description = "通过滑块验证后发送邮箱验证码，5分钟有效")
    public SaResult sendEmailCode(@Valid @RequestBody EmailCodeDTO dto) {
        captchaService.sendEmailCode(dto);
        return SaResult.ok();
    }
}
