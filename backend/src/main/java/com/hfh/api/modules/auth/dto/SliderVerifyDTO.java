package com.hfh.api.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 滑块验证请求DTO
 */
@Data
@Schema(description = "滑块验证请求")
public class SliderVerifyDTO {

    @NotBlank(message = "token不能为空")
    @Schema(description = "滑块验证token", requiredMode = Schema.RequiredMode.REQUIRED)
    private String token;

    @NotNull(message = "滑块位置不能为空")
    @Schema(description = "滑块位置（像素）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer position;
}
