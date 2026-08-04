package com.hfh.api.modules.system.task;

import com.hfh.api.modules.system.entity.JobEntity;
import com.hfh.api.modules.system.service.JobLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@Component
public class JobSchedulingService {

    private final ThreadPoolTaskScheduler taskScheduler;
    private final JobTaskExecutor jobTaskExecutor;
    private final JobLogService jobLogService;

    public JobSchedulingService(ThreadPoolTaskScheduler taskScheduler,
                                JobTaskExecutor jobTaskExecutor,
                                JobLogService jobLogService) {
        this.taskScheduler = taskScheduler;
        this.jobTaskExecutor = jobTaskExecutor;
        this.jobLogService = jobLogService;
    }

    private final Map<Long, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    public void schedule(JobEntity job) {
        if (job == null || job.getId() == null || job.getStatus() == null || job.getStatus() != 1) {
            return;
        }
        String cron = job.getCronExpression();
        if (cron == null || cron.trim().isEmpty()) {
            log.error("定时任务 cron 表达式为空：{}", job.getJobName());
            return;
        }
        if (!CronExpression.isValidExpression(cron)) {
            log.error("定时任务 cron 表达式不合法：{}，cron：{}", job.getJobName(), cron);
            return;
        }
        cancel(job.getId());
        try {
            CronTrigger trigger = new CronTrigger(cron);
            boolean allowConcurrent = job.getConcurrent() != null && job.getConcurrent() == 0;
            ScheduledFuture<?> future = taskScheduler.schedule(
                    new JobRunnable(job, jobTaskExecutor, jobLogService, allowConcurrent),
                    trigger
            );
            scheduledTasks.put(job.getId(), future);
            log.info("定时任务注册成功：{}，cron：{}", job.getJobName(), cron);
        } catch (Exception e) {
            log.error("定时任务注册失败：{}，cron：{}", job.getJobName(), cron, e);
        }
    }

    public void cancel(Long jobId) {
        ScheduledFuture<?> future = scheduledTasks.remove(jobId);
        if (future != null && !future.isCancelled()) {
            future.cancel(false);
        }
    }

    public void executeOnce(JobEntity job) {
        boolean allowConcurrent = job.getConcurrent() != null && job.getConcurrent() == 0;
        new JobRunnable(job, jobTaskExecutor, jobLogService, allowConcurrent).run();
    }
}
