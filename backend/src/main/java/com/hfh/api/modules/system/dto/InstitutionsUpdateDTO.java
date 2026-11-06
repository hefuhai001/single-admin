package com.hfh.api.modules.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class InstitutionsUpdateDTO {

    @NotNull(message = "机构ID不能为空")
    private Long id;

    private Long parentId;

    @NotBlank(message = "机构编码不能为空")
    @Size(max = 30, message = "机构编码长度不能超过 30 个字符")
    private String institutionCode;

    @NotBlank(message = "机构名称不能为空")
    @Size(max = 100, message = "机构名称长度不能超过 100 个字符")
    private String institutionName;

    private Byte status;
}
