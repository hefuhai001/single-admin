package com.hfh.api.modules.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ConfigSaveDTO {

    @NotBlank(message = "配置键不能为空")
    @Size(max = 100, message = "配置键长度不能超过 100 个字符")
    private String configKey;

    private String configValue;

    @Size(max = 200, message = "配置名称长度不能超过 200 个字符")
    private String configName;

    @Size(max = 50, message = "配置分组长度不能超过 50 个字符")
    private String configGroup;

    @Size(max = 20, message = "配置类型长度不能超过 20 个字符")
    private String configType;

    private Boolean isSystem;

    private Boolean status;

    @Size(max = 500, message = "备注说明长度不能超过 500 个字符")
    private String remark;
}
