package com.hfh.api.modules.document.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hfh.api.base.BaseController;
import com.hfh.api.modules.document.dto.FeedbacksSaveDTO;
import com.hfh.api.modules.document.dto.FeedbacksUpdateDTO;
import com.hfh.api.modules.document.entity.FeedbacksEntity;
import com.hfh.api.modules.document.service.FeedbacksService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.hfh.api.utils.BeanMapper;
import org.springframework.web.bind.annotation.*;
import java.util.Date;


@RestController(value = "feedbacksEntityController")
@RequestMapping("/admin/feedbacks")
@RequiredArgsConstructor
@Tag(name = "机构反馈表管理", description = "机构反馈表增删改查接口")
public class FeedbacksController extends BaseController {

    private final FeedbacksService feedbacksService;

    /**
     * 根据ID查询机构反馈表
     */
    @SaCheckPermission("feedbacks:query")
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询机构反馈表")
    public SaResult getFeedbacksById(@PathVariable("id") Long id) {
        FeedbacksEntity entity = feedbacksService.getById(id);
        if (entity == null) {
            return SaResult.error("机构反馈表不存在");
        }
        return SaResult.data(entity);
    }

    /**
     * 查询所有机构反馈表
     */
    @SaCheckPermission("feedbacks:query")
    @GetMapping("/list")
    @Operation(summary = "查询所有机构反馈表")
    public SaResult listFeedbacks() {
        QueryWrapper<FeedbacksEntity> wrapper = new QueryWrapper<>();
        wrapper.orderByAsc("id");
        return SaResult.data(feedbacksService.list(wrapper));
    }

    /**
     * 分页查询机构反馈表
     */
    @SaCheckPermission("feedbacks:query")
    @GetMapping("/page")
    @Operation(summary = "分页查询机构反馈表")
    public SaResult pageFeedbacks(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "反馈ID") @RequestParam(required = false) Long id,
            @Parameter(description = "机构ID") @RequestParam(required = false) Long institutionId,
            @Parameter(description = "反馈类型: question-疑问, suggestion-建议, complaint-投诉, issue-问题报告, other-其他") @RequestParam(required = false) String feedbackType,
            @Parameter(description = "反馈标题") @RequestParam(required = false) String feedbackTitle,
            @Parameter(description = "反馈内容") @RequestParam(required = false) String feedbackContent,
            @Parameter(description = "附件URL") @RequestParam(required = false) String attachmentUrl,
            @Parameter(description = "优先级: low-低, medium-中, high-高, urgent-紧急") @RequestParam(required = false) String priority,
            @Parameter(description = "状态: pending-待处理, processing-处理中, resolved-已解决, closed-已关闭, rejected-已驳回") @RequestParam(required = false) String status,
            @Parameter(description = "回复内容") @RequestParam(required = false) String replyContent,
            @Parameter(description = "回复人ID") @RequestParam(required = false) Long repliedBy,
            @Parameter(description = "反馈人ID") @RequestParam(required = false) Long feedbackBy
    ) {
        Page<FeedbacksEntity> page = new Page<>(current, size);
        QueryWrapper<FeedbacksEntity> wrapper = new QueryWrapper<>();
        wrapper.eq(id != null, "id", id);
        wrapper.eq(institutionId != null, "institution_id", institutionId);
        wrapper.like(feedbackType != null && !feedbackType.isEmpty(), "feedback_type", feedbackType);
        wrapper.like(feedbackTitle != null && !feedbackTitle.isEmpty(), "feedback_title", feedbackTitle);
        wrapper.like(feedbackContent != null && !feedbackContent.isEmpty(), "feedback_content", feedbackContent);
        wrapper.like(attachmentUrl != null && !attachmentUrl.isEmpty(), "attachment_url", attachmentUrl);
        wrapper.like(priority != null && !priority.isEmpty(), "priority", priority);
        wrapper.like(status != null && !status.isEmpty(), "status", status);
        wrapper.like(replyContent != null && !replyContent.isEmpty(), "reply_content", replyContent);
        wrapper.eq(repliedBy != null, "replied_by", repliedBy);
        wrapper.eq(feedbackBy != null, "feedback_by", feedbackBy);
        wrapper.orderByDesc("id");
        return SaResult.data(feedbacksService.page(page, wrapper));
    }

    /**
     * 新增机构反馈表
     */
    @SaCheckPermission("feedbacks:add")
    @PostMapping
    @Operation(summary = "新增机构反馈表")
    public SaResult saveFeedbacks(@Valid @RequestBody FeedbacksSaveDTO dto) {
        FeedbacksEntity entity = BeanMapper.map(dto, FeedbacksEntity.class);
        boolean success = feedbacksService.save(entity);
        return success ? SaResult.ok("新增成功") : SaResult.error("新增失败");
    }

    /**
     * 更新机构反馈表
     */
    @SaCheckPermission("feedbacks:update")
    @PutMapping
    @Operation(summary = "更新机构反馈表")
    public SaResult updateFeedbacks(@Valid @RequestBody FeedbacksUpdateDTO dto) {
        FeedbacksEntity entity = BeanMapper.map(dto, FeedbacksEntity.class);
        if (entity.getRepliedBy() != null && entity.getRepliedAt() == null) {
            entity.setRepliedAt(new Date());
        }
        boolean success = feedbacksService.updateById(entity);
        return success ? SaResult.ok("更新成功") : SaResult.error("更新失败");
    }

    /**
     * 删除机构反馈表
     */
    @SaCheckPermission("feedbacks:delete")
    @DeleteMapping("/{id}")
    @Operation(summary = "删除机构反馈表")
    public SaResult deleteFeedbacks(@PathVariable("id") Long id) {
        boolean success = feedbacksService.removeById(id);
        return success ? SaResult.ok("删除成功") : SaResult.error("删除失败");
    }

    /**
     * 批量删除机构反馈表
     */
    @SaCheckPermission("feedbacks:delete")
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除机构反馈表")
    public SaResult deleteFeedbacksBatch(@RequestBody java.util.List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return SaResult.error("请选择要删除的数据");
        }
        boolean success = feedbacksService.removeByIds(ids);
        return success ? SaResult.ok("批量删除成功") : SaResult.error("批量删除失败");
    }
}
