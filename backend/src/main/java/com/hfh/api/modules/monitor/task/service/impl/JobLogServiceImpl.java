package com.hfh.api.modules.monitor.task.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hfh.api.modules.monitor.task.entity.JobLogEntity;
import com.hfh.api.modules.monitor.task.mapper.JobLogMapper;
import com.hfh.api.modules.monitor.task.service.JobLogService;

import org.springframework.stereotype.Service;

@Service
public class JobLogServiceImpl extends ServiceImpl<JobLogMapper, JobLogEntity> implements JobLogService {
}
