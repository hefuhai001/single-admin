package com.hfh.api.modules.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 定时任务日志
 * </p>
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("sys_job_log")
public class JobLogEntity implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("job_name")
    private String jobName;

    @TableField("job_group")
    private String jobGroup;

    @TableField("invoke_target")
    private String invokeTarget;

    @TableField("job_message")
    private String jobMessage;

    @TableField("status")
    private Integer status;

    @TableField("exception_info")
    private String exceptionInfo;

    @TableField("create_time")
    private LocalDateTime createTime;
}
