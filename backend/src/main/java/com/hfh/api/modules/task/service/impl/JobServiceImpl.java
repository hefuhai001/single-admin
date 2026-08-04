package com.hfh.api.modules.task.service.impl;

import com.hfh.api.modules.task.entity.JobEntity;
import com.hfh.api.modules.task.mapper.JobMapper;
import com.hfh.api.modules.task.service.JobService;
import com.hfh.api.modules.task.task.JobSchedulingService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
@RequiredArgsConstructor
public class JobServiceImpl extends ServiceImpl<JobMapper, JobEntity> implements JobService {

    private final JobSchedulingService jobSchedulingService;

    @PostConstruct
    public void loadJobsOnStartup() {
        list().forEach(job -> {
            if (job.getStatus() != null && job.getStatus() == 1) {
                jobSchedulingService.schedule(job);
            }
        });
    }

    @Override
    public boolean addJob(JobEntity job) {
        boolean saved = save(job);
        if (saved && job.getStatus() != null && job.getStatus() == 1) {
            jobSchedulingService.schedule(job);
        }
        return saved;
    }

    @Override
    public boolean updateJob(JobEntity job) {
        jobSchedulingService.cancel(job.getId());
        boolean updated = updateById(job);
        if (updated && job.getStatus() != null && job.getStatus() == 1) {
            jobSchedulingService.schedule(job);
        }
        return updated;
    }

    @Override
    public boolean deleteJob(Long id) {
        jobSchedulingService.cancel(id);
        return removeById(id);
    }

    @Override
    public boolean changeStatus(Long id, Integer status) {
        JobEntity job = getById(id);
        if (job == null) {
            return false;
        }
        job.setStatus(status);
        jobSchedulingService.cancel(id);
        boolean updated = updateById(job);
        if (updated && status != null && status == 1) {
            jobSchedulingService.schedule(job);
        }
        return updated;
    }

    @Override
    public boolean runOnce(Long id) {
        JobEntity job = getById(id);
        if (job == null) {
            return false;
        }
        jobSchedulingService.executeOnce(job);
        return true;
    }
}
