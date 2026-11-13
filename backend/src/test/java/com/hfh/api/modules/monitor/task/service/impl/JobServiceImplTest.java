package com.hfh.api.modules.monitor.task.service.impl;

import com.hfh.api.modules.monitor.task.entity.JobEntity;
import com.hfh.api.modules.monitor.task.mapper.JobMapper;
import com.hfh.api.modules.monitor.task.task.JobSchedulingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JobServiceImplTest {

    @Mock
    private JobMapper jobMapper;
    @Mock
    private JobSchedulingService jobSchedulingService;

    private JobServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new JobServiceImpl(jobSchedulingService);
        ReflectionTestUtils.setField(service, "baseMapper", jobMapper);
    }

    private JobEntity job(Long id, Integer status) {
        return new JobEntity().setId(id).setJobName("job" + id).setStatus(status);
    }

    @Test
    void startupShouldScheduleOnlyEnabledJobs() {
        when(jobMapper.selectList(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class)))
                .thenReturn(List.of(job(1L, 1), job(2L, 0), job(3L, null)));

        service.loadJobsOnStartup();

        verify(jobSchedulingService).schedule(any(JobEntity.class));
        verify(jobSchedulingService, org.mockito.Mockito.times(1)).schedule(any());
    }

    @Test
    void addJobShouldScheduleWhenEnabled() {
        when(jobMapper.insert(any(JobEntity.class))).thenReturn(1);

        assertThat(service.addJob(job(1L, 1))).isTrue();
        verify(jobSchedulingService).schedule(any(JobEntity.class));

        assertThat(service.addJob(job(2L, 0))).isTrue();
        verify(jobSchedulingService, org.mockito.Mockito.times(1)).schedule(any());
    }

    @Test
    void addJobShouldNotScheduleWhenInsertFailed() {
        when(jobMapper.insert(any(JobEntity.class))).thenReturn(0);

        assertThat(service.addJob(job(1L, 1))).isFalse();
        verify(jobSchedulingService, never()).schedule(any());
    }

    @Test
    void updateJobShouldCancelThenReschedule() {
        when(jobMapper.updateById(any(JobEntity.class))).thenReturn(1);

        assertThat(service.updateJob(job(1L, 1))).isTrue();
        verify(jobSchedulingService).cancel(1L);
        verify(jobSchedulingService).schedule(any(JobEntity.class));

        assertThat(service.updateJob(job(2L, 0))).isTrue();
        verify(jobSchedulingService, org.mockito.Mockito.times(1)).schedule(any());
    }

    @Test
    void deleteJobShouldCancelAndRemove() {
        when(jobMapper.deleteById(1L)).thenReturn(1);

        assertThat(service.deleteJob(1L)).isTrue();
        verify(jobSchedulingService).cancel(1L);
    }

    @Test
    void changeStatusShouldHandleMissingJob() {
        when(jobMapper.selectById(9L)).thenReturn(null);

        assertThat(service.changeStatus(9L, 1)).isFalse();
        verify(jobSchedulingService, never()).cancel(any());
    }

    @Test
    void changeStatusShouldUpdateAndReschedule() {
        when(jobMapper.selectById(1L)).thenReturn(job(1L, 0));
        when(jobMapper.updateById(any(JobEntity.class))).thenReturn(1);

        assertThat(service.changeStatus(1L, 1)).isTrue();
        verify(jobSchedulingService).cancel(1L);
        verify(jobSchedulingService).schedule(any(JobEntity.class));
    }

    @Test
    void runOnceShouldExecuteExistingJobOnly() {
        when(jobMapper.selectById(1L)).thenReturn(job(1L, 1));
        when(jobMapper.selectById(2L)).thenReturn(null);

        assertThat(service.runOnce(1L)).isTrue();
        assertThat(service.runOnce(2L)).isFalse();
        verify(jobSchedulingService).executeOnce(any(JobEntity.class));
    }

    @Test
    void jobLogServiceShouldBeInstantiable() {
        assertThat(new JobLogServiceImpl()).isNotNull();
    }
}
