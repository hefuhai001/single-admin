package com.hfh.api.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 滑块验证码响应VO
 */
@Data
@Schema(description = "滑块验证码响应")
public class SliderCaptchaVO {

    @Schema(description = "验证token")
    private String token;

    @Schema(description = "背景图片（base64）")
    private String backgroundImage;

    @Schema(description = "滑块图片（base64）")
    private String sliderImage;

    @Schema(description = "滑块Y轴位置")
    private int sliderY;
}
