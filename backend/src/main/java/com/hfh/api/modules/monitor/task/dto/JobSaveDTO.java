package com.hfh.api.modules.monitor.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class JobSaveDTO {

    @NotBlank(message = "任务名称不能为空")
    @Size(max = 100, message = "任务名称长度不能超过 100 个字符")
    private String jobName;

    @Size(max = 50, message = "任务分组长度不能超过 50 个字符")
    private String jobGroup;

    @NotBlank(message = "调用目标字符串不能为空")
    @Size(max = 255, message = "调用目标字符串长度不能超过 255 个字符")
    private String invokeTarget;

    @NotBlank(message = "cron执行表达式不能为空")
    @Size(max = 50, message = "cron执行表达式长度不能超过 50 个字符")
    private String cronExpression;

    private Integer misfirePolicy;

    private Integer concurrent;

    private Integer status;

    @Size(max = 500, message = "备注信息长度不能超过 500 个字符")
    private String remark;
}
