package com.hfh.api.modules.task.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hfh.api.base.BaseController;
import com.hfh.api.modules.task.entity.JobLogEntity;
import com.hfh.api.modules.task.service.JobLogService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController(value = "jobLogEntityController")
@RequestMapping("/system/jobLog")
@RequiredArgsConstructor
@Tag(name = "定时任务日志", description = "定时任务执行日志接口")
public class JobLogController extends BaseController {

    private final JobLogService jobLogService;

    @SaCheckPermission("job:query")
    @GetMapping("/page")
    @Operation(summary = "分页查询定时任务日志")
    public SaResult pageJobLog(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String jobName,
            @RequestParam(required = false) Integer status) {
        Page<JobLogEntity> page = new Page<>(current, size);
        LambdaQueryWrapper<JobLogEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(jobName != null && !jobName.isEmpty(), JobLogEntity::getJobName, jobName)
                .eq(status != null, JobLogEntity::getStatus, status)
                .orderByDesc(JobLogEntity::getCreateTime);
        return SaResult.data(jobLogService.page(page, wrapper));
    }

    @SaCheckPermission("job:delete")
    @DeleteMapping("/clean")
    @Operation(summary = "清空定时任务日志")
    public SaResult clean() {
        boolean success = jobLogService.remove(new LambdaQueryWrapper<>());
        return success ? SaResult.ok("清空成功") : SaResult.error("清空失败");
    }
}
