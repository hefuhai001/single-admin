package com.hfh.api.modules.monitor.task.task;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

import com.hfh.api.modules.monitor.task.entity.JobEntity;
import com.hfh.api.modules.monitor.task.entity.JobLogEntity;
import com.hfh.api.modules.monitor.task.service.JobLogService;

@RequiredArgsConstructor
public class JobRunnable implements Runnable {

    private final JobEntity job;
    private final JobTaskExecutor executor;
    private final JobLogService jobLogService;
    private final boolean allowConcurrent;

    @Override
    public void run() {
        if (!allowConcurrent) {
            doRun();
        } else {
            synchronized (getLockKey().intern()) {
                doRun();
            }
        }
    }

    private void doRun() {
        JobLogEntity log = new JobLogEntity()
                .setJobName(job.getJobName())
                .setJobGroup(job.getJobGroup())
                .setInvokeTarget(job.getInvokeTarget())
                .setCreateTime(LocalDateTime.now());
        try {
            executor.execute(job.getInvokeTarget());
            log.setJobMessage("执行成功")
                    .setStatus(1);
        } catch (Exception e) {
            log.setJobMessage("执行失败：" + e.getMessage())
                    .setStatus(0)
                    .setExceptionInfo(getStackTrace(e));
        }
        jobLogService.save(log);
    }

    private String getLockKey() {
        return job.getJobName() + "@" + job.getJobGroup();
    }

    private String getStackTrace(Exception e) {
        StringBuilder sb = new StringBuilder();
        sb.append(e).append("\n");
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append("\tat ").append(element).append("\n");
        }
        return sb.toString();
    }
}
