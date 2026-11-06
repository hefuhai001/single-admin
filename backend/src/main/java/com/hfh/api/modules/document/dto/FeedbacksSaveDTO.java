package com.hfh.api.modules.document.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Date;

@Data
public class FeedbacksSaveDTO {

    @NotNull(message = "机构ID不能为空")
    private Long institutionId;

    @NotBlank(message = "反馈类型不能为空")
    @Size(max = 30, message = "反馈类型长度不能超过 30 个字符")
    private String feedbackType;

    @NotBlank(message = "反馈标题不能为空")
    @Size(max = 200, message = "反馈标题长度不能超过 200 个字符")
    private String feedbackTitle;

    @NotBlank(message = "反馈内容不能为空")
    private String feedbackContent;

    @Size(max = 500, message = "附件URL长度不能超过 500 个字符")
    private String attachmentUrl;

    @Size(max = 20, message = "优先级长度不能超过 20 个字符")
    private String priority;

    @Size(max = 20, message = "状态长度不能超过 20 个字符")
    private String status;

    private String replyContent;

    private Long repliedBy;

    private Date repliedAt;

    private Long feedbackBy;
}
