package com.hfh.api.modules.system.service.impl;

import com.hfh.api.modules.system.entity.JobLogEntity;
import com.hfh.api.modules.system.mapper.JobLogMapper;
import com.hfh.api.modules.system.service.JobLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class JobLogServiceImpl extends ServiceImpl<JobLogMapper, JobLogEntity> implements JobLogService {
}
