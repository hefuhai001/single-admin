package com.hfh.api.modules.document.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Date;

@Data
public class GuidesUpdateDTO {

    @NotNull(message = "指南ID不能为空")
    private Long id;

    @NotNull(message = "机构ID不能为空")
    private Long institutionId;

    @NotBlank(message = "指南标题不能为空")
    @Size(max = 200, message = "指南标题长度不能超过 200 个字符")
    private String guideTitle;

    private String guideContent;

    @Size(max = 500, message = "指南附件URL长度不能超过 500 个字符")
    private String guideFileUrl;

    @Size(max = 30, message = "指南类型长度不能超过 30 个字符")
    private String guideType;

    @Size(max = 20, message = "状态长度不能超过 20 个字符")
    private String status;

    private Date publishedAt;
}
