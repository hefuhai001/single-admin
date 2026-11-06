package com.hfh.api.modules.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DictDataUpdateDTO {

    @NotNull(message = "字典数据ID不能为空")
    private Long id;

    @NotBlank(message = "所属字典类型不能为空")
    @Size(max = 100, message = "所属字典类型长度不能超过 100 个字符")
    private String dictType;

    @NotBlank(message = "显示标签不能为空")
    @Size(max = 100, message = "显示标签长度不能超过 100 个字符")
    private String dictLabel;

    @NotBlank(message = "存储值不能为空")
    @Size(max = 100, message = "存储值长度不能超过 100 个字符")
    private String dictValue;

    private Integer dictSort;

    private Boolean status;

    private Boolean isDefault;

    @Size(max = 100, message = "样式属性长度不能超过 100 个字符")
    private String cssClass;

    @Size(max = 100, message = "表格列表样式长度不能超过 100 个字符")
    private String listClass;
}
