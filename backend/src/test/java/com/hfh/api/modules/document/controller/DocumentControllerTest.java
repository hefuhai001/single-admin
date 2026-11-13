package com.hfh.api.modules.document.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hfh.api.modules.document.dto.FeedbacksSaveDTO;
import com.hfh.api.modules.document.dto.FeedbacksUpdateDTO;
import com.hfh.api.modules.document.dto.GuidesSaveDTO;
import com.hfh.api.modules.document.dto.GuidesUpdateDTO;
import com.hfh.api.modules.document.entity.FeedbacksEntity;
import com.hfh.api.modules.document.entity.GuidesEntity;
import com.hfh.api.modules.document.service.FeedbacksService;
import com.hfh.api.modules.document.service.GuidesService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class DocumentControllerTest {

    private final FeedbacksService feedbacksService = mock(FeedbacksService.class);
    private final GuidesService guidesService = mock(GuidesService.class);
    private MockedStatic<StpUtil> stp;

    @BeforeEach
    void setUp() {
        stp = mockStatic(StpUtil.class);
        stp.when(StpUtil::isLogin).thenReturn(false);
    }

    @AfterEach
    void tearDown() {
        stp.close();
    }

    @Test
    void feedbacksControllerShouldHandleCrud() {
        FeedbacksController controller = new FeedbacksController(feedbacksService);
        when(feedbacksService.getById(1L)).thenReturn(null);
        when(feedbacksService.getById(2L)).thenReturn(new FeedbacksEntity());
        when(feedbacksService.list(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class))).thenReturn(List.of(new FeedbacksEntity()));
        when(feedbacksService.page(any(), any())).thenReturn(new Page<>());
        when(feedbacksService.save(any())).thenReturn(true);
        when(feedbacksService.updateById(any())).thenReturn(true);
        when(feedbacksService.removeById(1L)).thenReturn(true);
        when(feedbacksService.removeByIds(anyList())).thenReturn(true);

        assertThat(controller.getFeedbacksById(1L).getCode()).isNotEqualTo(200);
        assertThat(controller.getFeedbacksById(2L).getData()).isNotNull();
        assertThat((List<?>) controller.listFeedbacks().getData()).hasSize(1);
        assertThat(controller.pageFeedbacks(1, 10, 1L, 2L, "question", "title", "content", "url",
                "high", "pending", "reply", 3L, 4L).getData()).isNotNull();
        assertThat(controller.pageFeedbacks(1, 10, null, null, null, null, null, null,
                null, null, null, null, null).getData()).isNotNull();
        assertThat(controller.saveFeedbacks(new FeedbacksSaveDTO()).getMsg()).isEqualTo("新增成功");
        FeedbacksUpdateDTO dto = new FeedbacksUpdateDTO();
        dto.setRepliedBy(9L);
        assertThat(controller.updateFeedbacks(dto).getMsg()).isEqualTo("更新成功");
        assertThat(controller.deleteFeedbacks(1L).getMsg()).isEqualTo("删除成功");
        assertThat(controller.deleteFeedbacksBatch(null).getMsg()).isEqualTo("请选择要删除的数据");
        assertThat(controller.deleteFeedbacksBatch(List.of(1L)).getMsg()).isEqualTo("批量删除成功");
    }

    @Test
    void feedbacksFailuresShouldReturnErrors() {
        FeedbacksController controller = new FeedbacksController(feedbacksService);
        when(feedbacksService.save(any())).thenReturn(false);
        when(feedbacksService.updateById(any())).thenReturn(false);
        when(feedbacksService.removeById(1L)).thenReturn(false);
        when(feedbacksService.removeByIds(anyList())).thenReturn(false);

        assertThat(controller.saveFeedbacks(new FeedbacksSaveDTO()).getMsg()).isEqualTo("新增失败");
        assertThat(controller.updateFeedbacks(new FeedbacksUpdateDTO()).getMsg()).isEqualTo("更新失败");
        assertThat(controller.deleteFeedbacks(1L).getMsg()).isEqualTo("删除失败");
        assertThat(controller.deleteFeedbacksBatch(List.of(1L)).getMsg()).isEqualTo("批量删除失败");
    }

    @Test
    void guidesControllerShouldHandleCrud() {
        GuidesController controller = new GuidesController(guidesService);
        when(guidesService.getById(1L)).thenReturn(null);
        when(guidesService.getById(2L)).thenReturn(new GuidesEntity());
        when(guidesService.list(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class))).thenReturn(List.of(new GuidesEntity()));
        when(guidesService.page(any(), any())).thenReturn(new Page<>());
        when(guidesService.save(any())).thenReturn(true);
        when(guidesService.updateById(any())).thenReturn(true);
        when(guidesService.removeById(1L)).thenReturn(true);
        when(guidesService.removeByIds(anyList())).thenReturn(true);

        assertThat(controller.getGuidesById(1L).getCode()).isNotEqualTo(200);
        assertThat(controller.getGuidesById(2L).getData()).isNotNull();
        assertThat((List<?>) controller.listGuides().getData()).hasSize(1);
        assertThat(controller.pageGuides(1, 10, 1L, 2L, "t", "c", "u", "general", "active", 3L)
                .getData()).isNotNull();
        assertThat(controller.pageGuides(1, 10, null, null, null, null, null, null, null, null)
                .getData()).isNotNull();
        assertThat(controller.saveGuides(new GuidesSaveDTO()).getMsg()).isEqualTo("新增成功");
        assertThat(controller.updateGuides(new GuidesUpdateDTO()).getMsg()).isEqualTo("更新成功");
        assertThat(controller.deleteGuides(1L).getMsg()).isEqualTo("删除成功");
        assertThat(controller.deleteGuidesBatch(List.of()).getMsg()).isEqualTo("请选择要删除的数据");
        assertThat(controller.deleteGuidesBatch(List.of(1L)).getMsg()).isEqualTo("批量删除成功");
    }

    @Test
    void guidesFailuresShouldReturnErrors() {
        GuidesController controller = new GuidesController(guidesService);
        when(guidesService.save(any())).thenReturn(false);
        when(guidesService.updateById(any())).thenReturn(false);
        when(guidesService.removeById(1L)).thenReturn(false);
        when(guidesService.removeByIds(anyList())).thenReturn(false);

        assertThat(controller.saveGuides(new GuidesSaveDTO()).getMsg()).isEqualTo("新增失败");
        assertThat(controller.updateGuides(new GuidesUpdateDTO()).getMsg()).isEqualTo("更新失败");
        assertThat(controller.deleteGuides(1L).getMsg()).isEqualTo("删除失败");
        assertThat(controller.deleteGuidesBatch(List.of(1L)).getMsg()).isEqualTo("批量删除失败");
    }
}
