package com.hfh.api.modules.monitor.task.task;

import com.hfh.api.modules.monitor.task.entity.JobEntity;
import com.hfh.api.modules.monitor.task.entity.JobLogEntity;
import com.hfh.api.modules.monitor.task.service.JobLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.ScheduledFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JobSchedulingTest {

    @Mock
    private ThreadPoolTaskScheduler taskScheduler;
    @Mock
    private JobTaskExecutor jobTaskExecutor;
    @Mock
    private JobLogService jobLogService;

    private JobSchedulingService schedulingService;

    @BeforeEach
    void setUp() {
        schedulingService = new JobSchedulingService(taskScheduler, jobTaskExecutor, jobLogService);
    }

    private JobEntity job() {
        return new JobEntity().setId(1L).setJobName("demo").setJobGroup("DEFAULT")
                .setInvokeTarget("com.demo.Task#run").setCronExpression("0/5 * * * * ?")
                .setStatus(1).setConcurrent(0);
    }

    @Test
    void scheduleShouldIgnoreInvalidJob() {
        schedulingService.schedule(null);
        schedulingService.schedule(new JobEntity());
        schedulingService.schedule(new JobEntity().setId(1L).setStatus(0));
        schedulingService.schedule(new JobEntity().setId(1L).setStatus(1));
        schedulingService.schedule(job().setCronExpression("bad cron"));

        verify(taskScheduler, never()).schedule(any(Runnable.class), any(org.springframework.scheduling.Trigger.class));
    }

    @Test
    void scheduleShouldRegisterAndCancelPrevious() {
        @SuppressWarnings("rawtypes")
        ScheduledFuture future = Mockito.mock(ScheduledFuture.class);
        when(taskScheduler.schedule(any(Runnable.class), any(org.springframework.scheduling.Trigger.class)))
                .thenReturn(future);

        schedulingService.schedule(job());

        verify(taskScheduler).schedule(any(JobRunnable.class), any(org.springframework.scheduling.Trigger.class));

        when(future.isCancelled()).thenReturn(false);
        schedulingService.cancel(1L);
        verify(future).cancel(false);

        schedulingService.cancel(999L);
    }

    @Test
    void scheduleShouldSkipCancelWhenAlreadyCancelled() {
        @SuppressWarnings("rawtypes")
        ScheduledFuture future = Mockito.mock(ScheduledFuture.class);
        when(taskScheduler.schedule(any(Runnable.class), any(org.springframework.scheduling.Trigger.class)))
                .thenReturn(future);
        when(future.isCancelled()).thenReturn(true);
        schedulingService.schedule(job());

        schedulingService.cancel(1L);

        verify(future, never()).cancel(false);
    }

    @Test
    void scheduleShouldSwallowSchedulerFailure() {
        when(taskScheduler.schedule(any(Runnable.class), any(org.springframework.scheduling.Trigger.class)))
                .thenThrow(new RuntimeException("pool closed"));

        schedulingService.schedule(job());

        verify(taskScheduler).schedule(any(Runnable.class), any(org.springframework.scheduling.Trigger.class));
    }

    @Test
    void executeOnceShouldRunRunnableAndWriteLog() {
        JobEntity job = job().setConcurrent(1);

        schedulingService.executeOnce(job);

        verify(jobTaskExecutor).execute("com.demo.Task#run");
        ArgumentCaptor<JobLogEntity> captor = ArgumentCaptor.forClass(JobLogEntity.class);
        verify(jobLogService).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(1);
        assertThat(captor.getValue().getJobMessage()).isEqualTo("执行成功");
    }

    @Test
    void executeOnceShouldRecordFailure() {
        JobEntity job = job().setConcurrent(0);
        doThrow(new RuntimeException("boom")).when(jobTaskExecutor).execute(any());

        schedulingService.executeOnce(job);

        ArgumentCaptor<JobLogEntity> captor = ArgumentCaptor.forClass(JobLogEntity.class);
        verify(jobLogService).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isZero();
        assertThat(captor.getValue().getJobMessage()).contains("执行失败");
        assertThat(captor.getValue().getExceptionInfo()).contains("boom");
    }
}
