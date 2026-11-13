package com.hfh.api.modules.monitor.task.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hfh.api.modules.monitor.task.dto.JobSaveDTO;
import com.hfh.api.modules.monitor.task.dto.JobUpdateDTO;
import com.hfh.api.modules.monitor.task.entity.JobEntity;
import com.hfh.api.modules.monitor.task.service.JobLogService;
import com.hfh.api.modules.monitor.task.service.JobService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JobControllerTest {

    private final JobService jobService = mock(JobService.class);
    private final JobLogService jobLogService = mock(JobLogService.class);
    private final JobController controller = new JobController(jobService);
    private final JobLogController logController = new JobLogController(jobLogService);

    @Test
    void jobCrudShouldWork() {
        when(jobService.getById(1L)).thenReturn(null);
        when(jobService.getById(2L)).thenReturn(new JobEntity());
        when(jobService.list()).thenReturn(List.of(new JobEntity()));
        when(jobService.page(any(), any())).thenReturn(new Page<>());
        when(jobService.addJob(any())).thenReturn(true);
        when(jobService.updateJob(any())).thenReturn(true);
        when(jobService.deleteJob(1L)).thenReturn(true);
        when(jobService.removeByIds(anyList())).thenReturn(false);
        when(jobService.changeStatus(1L, 1)).thenReturn(true);
        when(jobService.runOnce(1L)).thenReturn(true);

        assertThat(controller.getJobById(1L).getCode()).isNotEqualTo(200);
        assertThat(controller.getJobById(2L).getData()).isNotNull();
        assertThat((List<?>) controller.listJob().getData()).hasSize(1);
        assertThat(controller.pageJob(1L, 10L, "n", "g", 1).getData()).isNotNull();
        assertThat(controller.pageJob(1L, 10L, null, "", null).getData()).isNotNull();
        assertThat(controller.saveJob(new JobSaveDTO()).getMsg()).isEqualTo("新增成功");
        assertThat(controller.updateJob(new JobUpdateDTO()).getMsg()).isEqualTo("更新成功");
        assertThat(controller.deleteJob(1L).getMsg()).isEqualTo("删除成功");
        assertThat(controller.deleteJobBatch(null).getMsg()).isEqualTo("请选择要删除的数据");
        assertThat(controller.deleteJobBatch(List.of(1L)).getMsg()).isEqualTo("批量删除失败");
        assertThat(controller.changeStatus(1L, 1).getMsg()).isEqualTo("状态更新成功");
        assertThat(controller.run(1L).getMsg()).isEqualTo("执行成功");
    }

    @Test
    void jobFailuresShouldReturnErrors() {
        when(jobService.addJob(any())).thenReturn(false);
        when(jobService.updateJob(any())).thenReturn(false);
        when(jobService.deleteJob(1L)).thenReturn(false);
        when(jobService.changeStatus(1L, 0)).thenReturn(false);
        when(jobService.runOnce(1L)).thenReturn(false);

        assertThat(controller.saveJob(new JobSaveDTO()).getMsg()).isEqualTo("新增失败");
        assertThat(controller.updateJob(new JobUpdateDTO()).getMsg()).isEqualTo("更新失败");
        assertThat(controller.deleteJob(1L).getMsg()).isEqualTo("删除失败");
        assertThat(controller.changeStatus(1L, 0).getMsg()).isEqualTo("状态更新失败");
        assertThat(controller.run(1L).getMsg()).isEqualTo("执行失败");
    }

    @Test
    void jobLogControllerShouldPageAndClean() {
        when(jobLogService.page(any(), any())).thenReturn(new Page<>());
        when(jobLogService.remove(any())).thenReturn(true);

        assertThat(logController.pageJobLog(1L, 10L, "n", 1).getData()).isNotNull();
        assertThat(logController.pageJobLog(1L, 10L, "", null).getData()).isNotNull();
        assertThat(logController.clean().getMsg()).isEqualTo("清空成功");

        when(jobLogService.remove(any())).thenReturn(false);
        assertThat(logController.clean().getMsg()).isEqualTo("清空失败");
    }
}
