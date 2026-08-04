package com.hfh.api.modules.task.service.impl;

import com.hfh.api.modules.task.entity.JobLogEntity;
import com.hfh.api.modules.task.mapper.JobLogMapper;
import com.hfh.api.modules.task.service.JobLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class JobLogServiceImpl extends ServiceImpl<JobLogMapper, JobLogEntity> implements JobLogService {
}
