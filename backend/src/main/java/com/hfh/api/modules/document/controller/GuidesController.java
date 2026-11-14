package com.hfh.api.modules.document.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hfh.api.base.BaseController;
import com.hfh.api.modules.document.dto.GuidesSaveDTO;
import com.hfh.api.modules.document.dto.GuidesUpdateDTO;
import com.hfh.api.modules.document.entity.GuidesEntity;
import com.hfh.api.modules.document.service.GuidesService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.hfh.api.utils.BeanMapper;
import org.springframework.web.bind.annotation.*;
import java.util.Date;

@RestController(value = "guidesEntityController")
@RequestMapping("/admin/guides")
@RequiredArgsConstructor
@Tag(name = "机构指南表管理", description = "机构指南表增删改查接口")
public class GuidesController extends BaseController {

    private final GuidesService guidesService;

    /**
     * 根据ID查询机构指南表
     */
    @SaCheckPermission("guides:query")
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询机构指南表")
    public SaResult getGuidesById(@PathVariable("id") Long id) {
        GuidesEntity entity = guidesService.getById(id);
        if (entity == null) {
            return SaResult.error("机构指南表不存在");
        }
        return SaResult.data(entity);
    }

    /**
     * 查询所有机构指南表
     */
    @SaCheckPermission("guides:query")
    @GetMapping("/list")
    @Operation(summary = "查询所有机构指南表")
    public SaResult listGuides() {
        QueryWrapper<GuidesEntity> wrapper = new QueryWrapper<>();
        wrapper.orderByAsc("id");
        return SaResult.data(guidesService.list(wrapper));
    }

    /**
     * 分页查询机构指南表
     */
    @SaCheckPermission("guides:query")
    @GetMapping("/page")
    @Operation(summary = "分页查询机构指南表")
    public SaResult pageGuides(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "指南ID") @RequestParam(required = false) Long id,
            @Parameter(description = "机构ID") @RequestParam(required = false) Long institutionId,
            @Parameter(description = "指南标题") @RequestParam(required = false) String guideTitle,
            @Parameter(description = "指南内容(富文本)") @RequestParam(required = false) String guideContent,
            @Parameter(description = "指南附件URL(PDF/Word等)") @RequestParam(required = false) String guideFileUrl,
            @Parameter(description = "指南类型: general-通用指南, operation-操作指南, quality-质控指南, other-其他") @RequestParam(required = false) String guideType,
            @Parameter(description = "状态: active-有效, expired-已过期, archived-已归档") @RequestParam(required = false) String status,
            @Parameter(description = "发布人ID") @RequestParam(required = false) Long createdBy
    ) {
        Page<GuidesEntity> page = new Page<>(current, size);
        QueryWrapper<GuidesEntity> wrapper = new QueryWrapper<>();
        wrapper.eq(id != null, "id", id);
        wrapper.eq(institutionId != null, "institution_id", institutionId);
        wrapper.like(guideTitle != null && !guideTitle.isEmpty(), "guide_title", guideTitle);
        wrapper.like(guideContent != null && !guideContent.isEmpty(), "guide_content", guideContent);
        wrapper.like(guideFileUrl != null && !guideFileUrl.isEmpty(), "guide_file_url", guideFileUrl);
        wrapper.like(guideType != null && !guideType.isEmpty(), "guide_type", guideType);
        wrapper.like(status != null && !status.isEmpty(), "status", status);
        wrapper.eq(createdBy != null, "created_by", createdBy);
        wrapper.orderByDesc("id");
        return SaResult.data(guidesService.page(page, wrapper));
    }

    /**
     * 新增机构指南表
     */
    @SaCheckPermission("guides:add")
    @PostMapping
    @Operation(summary = "新增机构指南表")
    public SaResult saveGuides(@Valid @RequestBody GuidesSaveDTO dto) {
        GuidesEntity entity = BeanMapper.map(dto, GuidesEntity.class);
        if (entity.getCreatedBy() == null) {
            entity.setCreatedBy(getCurrentUserId());
        }
        if (entity.getPublishedAt() == null) {
            entity.setPublishedAt(new Date());
        }
        boolean success = guidesService.save(entity);
        return success ? SaResult.ok("新增成功") : SaResult.error("新增失败");
    }

    /**
     * 更新机构指南表
     */
    @SaCheckPermission("guides:update")
    @PutMapping
    @Operation(summary = "更新机构指南表")
    public SaResult updateGuides(@Valid @RequestBody GuidesUpdateDTO dto) {
        GuidesEntity entity = BeanMapper.map(dto, GuidesEntity.class);
        boolean success = guidesService.updateById(entity);
        return success ? SaResult.ok("更新成功") : SaResult.error("更新失败");
    }

    /**
     * 删除机构指南表
     */
    @SaCheckPermission("guides:delete")
    @DeleteMapping("/{id}")
    @Operation(summary = "删除机构指南表")
    public SaResult deleteGuides(@PathVariable("id") Long id) {
        boolean success = guidesService.removeById(id);
        return success ? SaResult.ok("删除成功") : SaResult.error("删除失败");
    }

    /**
     * 批量删除机构指南表
     */
    @SaCheckPermission("guides:delete")
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除机构指南表")
    public SaResult deleteGuidesBatch(@RequestBody java.util.List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return SaResult.error("请选择要删除的数据");
        }
        boolean success = guidesService.removeByIds(ids);
        return success ? SaResult.ok("批量删除成功") : SaResult.error("批量删除失败");
    }
}
