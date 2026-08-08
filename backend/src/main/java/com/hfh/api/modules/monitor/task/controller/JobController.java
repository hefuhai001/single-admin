package com.hfh.api.modules.monitor.task.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hfh.api.base.BaseController;
import com.hfh.api.modules.monitor.task.entity.JobEntity;
import com.hfh.api.modules.monitor.task.service.JobService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController(value = "jobEntityController")
@RequestMapping("/system/job")
@RequiredArgsConstructor
@Tag(name = "定时任务管理", description = "定时任务配置接口")
public class JobController extends BaseController {

    private final JobService jobService;

    @SaCheckPermission("job:query")
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询定时任务")
    public SaResult getJobById(@PathVariable("id") Long id) {
        JobEntity entity = jobService.getById(id);
        if (entity == null) {
            return SaResult.error("定时任务不存在");
        }
        return SaResult.data(entity);
    }

    @SaCheckPermission("job:query")
    @GetMapping("/list")
    @Operation(summary = "查询所有定时任务")
    public SaResult listJob() {
        return SaResult.data(jobService.list());
    }

    @SaCheckPermission("job:query")
    @GetMapping("/page")
    @Operation(summary = "分页查询定时任务")
    public SaResult pageJob(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String jobName,
            @RequestParam(required = false) String jobGroup,
            @RequestParam(required = false) Integer status) {
        Page<JobEntity> page = new Page<>(current, size);
        LambdaQueryWrapper<JobEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(jobName != null && !jobName.isEmpty(), JobEntity::getJobName, jobName)
                .like(jobGroup != null && !jobGroup.isEmpty(), JobEntity::getJobGroup, jobGroup)
                .eq(status != null, JobEntity::getStatus, status)
                .orderByDesc(JobEntity::getCreateTime);
        return SaResult.data(jobService.page(page, wrapper));
    }

    @SaCheckPermission("job:add")
    @PostMapping
    @Operation(summary = "新增定时任务")
    public SaResult saveJob(@RequestBody JobEntity job) {
        boolean success = jobService.addJob(job);
        return success ? SaResult.ok("新增成功") : SaResult.error("新增失败");
    }

    @SaCheckPermission("job:update")
    @PutMapping
    @Operation(summary = "更新定时任务")
    public SaResult updateJob(@RequestBody JobEntity job) {
        boolean success = jobService.updateJob(job);
        return success ? SaResult.ok("更新成功") : SaResult.error("更新失败");
    }

    @SaCheckPermission("job:delete")
    @DeleteMapping("/{id}")
    @Operation(summary = "删除定时任务")
    public SaResult deleteJob(@PathVariable("id") Long id) {
        boolean success = jobService.deleteJob(id);
        return success ? SaResult.ok("删除成功") : SaResult.error("删除失败");
    }

    @SaCheckPermission("job:delete")
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除定时任务")
    public SaResult deleteJobBatch(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return SaResult.error("请选择要删除的数据");
        }
        boolean success = jobService.removeByIds(ids);
        return success ? SaResult.ok("批量删除成功") : SaResult.error("批量删除失败");
    }

    @SaCheckPermission("job:update")
    @PutMapping("/changeStatus")
    @Operation(summary = "修改定时任务状态")
    public SaResult changeStatus(@RequestParam Long id, @RequestParam Integer status) {
        boolean success = jobService.changeStatus(id, status);
        return success ? SaResult.ok("状态更新成功") : SaResult.error("状态更新失败");
    }

    @SaCheckPermission("job:update")
    @PostMapping("/run/{id}")
    @Operation(summary = "立即执行一次定时任务")
    public SaResult run(@PathVariable("id") Long id) {
        boolean success = jobService.runOnce(id);
        return success ? SaResult.ok("执行成功") : SaResult.error("执行失败");
    }
}
