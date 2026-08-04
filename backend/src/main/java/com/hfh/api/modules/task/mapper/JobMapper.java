package com.hfh.api.modules.task.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hfh.api.modules.task.entity.JobEntity;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface JobMapper extends BaseMapper<JobEntity> {
}
