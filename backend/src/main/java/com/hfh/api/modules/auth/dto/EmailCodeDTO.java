package com.hfh.api.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 发送邮箱验证码请求DTO
 */
@Data
@Schema(description = "发送邮箱验证码请求")
public class EmailCodeDTO {

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱地址", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "滑块验证token不能为空")
    @Schema(description = "滑块验证通过后返回的token", requiredMode = Schema.RequiredMode.REQUIRED)
    private String captchaToken;
}
