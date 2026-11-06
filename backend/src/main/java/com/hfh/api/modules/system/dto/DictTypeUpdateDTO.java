package com.hfh.api.modules.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DictTypeUpdateDTO {

    @NotNull(message = "字典类型ID不能为空")
    private Long id;

    @NotBlank(message = "字典名称不能为空")
    @Size(max = 100, message = "字典名称长度不能超过 100 个字符")
    private String dictName;

    @NotBlank(message = "字典类型不能为空")
    @Size(max = 100, message = "字典类型长度不能超过 100 个字符")
    private String dictType;

    private Boolean status;
}
