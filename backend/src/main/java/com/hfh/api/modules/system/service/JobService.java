package com.hfh.api.modules.system.service;

import com.hfh.api.modules.system.entity.JobEntity;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 定时任务 服务类
 * </p>
 */
public interface JobService extends IService<JobEntity> {

    /**
     * 新增并注册定时任务
     */
    boolean addJob(JobEntity job);

    /**
     * 更新并刷新定时任务
     */
    boolean updateJob(JobEntity job);

    /**
     * 删除并取消定时任务
     */
    boolean deleteJob(Long id);

    /**
     * 修改任务状态
     */
    boolean changeStatus(Long id, Integer status);

    /**
     * 立即执行一次
     */
    boolean runOnce(Long id);
}
