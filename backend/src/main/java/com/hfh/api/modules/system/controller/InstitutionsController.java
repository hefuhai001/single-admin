package com.hfh.api.modules.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hfh.api.base.BaseController;
import com.hfh.api.modules.system.dto.InstitutionsSaveDTO;
import com.hfh.api.modules.system.dto.InstitutionsUpdateDTO;
import com.hfh.api.modules.system.entity.InstitutionsEntity;
import com.hfh.api.modules.system.service.InstitutionsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.hfh.api.utils.BeanMapper;
import org.springframework.web.bind.annotation.*;

@RestController(value = "institutionsEntityController")
@RequestMapping("/admin/institutions")
@RequiredArgsConstructor
@Tag(name = "机构管理", description = "机构增删改查接口")
public class InstitutionsController extends BaseController {

    private final InstitutionsService institutionsService;

    @SaCheckPermission("institutions:list")
    @GetMapping("/list-tree")
    @Operation(summary = "查询机构树")
    public SaResult treeInstitutions() {
        return SaResult.data(institutionsService.listTree());
    }

    /**
     * 根据ID查询机构
     */
    @SaCheckPermission("institutions:query")
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询机构")
    public SaResult getInstitutionsById(@PathVariable("id") Long id) {
        InstitutionsEntity entity = institutionsService.getById(id);
        if (entity == null) {
            return SaResult.error("机构不存在");
        }
        return SaResult.data(entity);
    }

    /**
     * 查询所有机构
     */
    @SaCheckPermission("institutions:query")
    @GetMapping("/list")
    @Operation(summary = "查询所有机构")
    public SaResult listInstitutions() {
        QueryWrapper<InstitutionsEntity> wrapper = new QueryWrapper<>();
        wrapper.orderByAsc("id");
        return SaResult.data(institutionsService.list(wrapper));
    }

    /**
     * 分页查询机构
     */
    @SaCheckPermission("institutions:query")
    @GetMapping("/page")
    @Operation(summary = "分页查询机构")
    public SaResult pageInstitutions(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "机构ID") @RequestParam(required = false) Long id,
            @Parameter(description = "父机构ID(上级机构)") @RequestParam(required = false) Long parentId,
            @Parameter(description = "机构编码") @RequestParam(required = false) String institutionCode,
            @Parameter(description = "机构名称") @RequestParam(required = false) String institutionName
    ) {
        Page<InstitutionsEntity> page = new Page<>(current, size);
        QueryWrapper<InstitutionsEntity> wrapper = new QueryWrapper<>();
        wrapper.eq(id != null, "id", id);
        wrapper.eq(parentId != null, "parent_id", parentId);
        wrapper.like(institutionCode != null && !institutionCode.isEmpty(), "institution_code", institutionCode);
        wrapper.like(institutionName != null && !institutionName.isEmpty(), "institution_name", institutionName);
        wrapper.orderByDesc("id");
        return SaResult.data(institutionsService.page(page, wrapper));
    }

    /**
     * 新增机构
     */
    @SaCheckPermission("institutions:add")
    @PostMapping
    @Operation(summary = "新增机构")
    public SaResult saveInstitutions(@Valid @RequestBody InstitutionsSaveDTO dto) {
        InstitutionsEntity entity = BeanMapper.map(dto, InstitutionsEntity.class);
        boolean success = institutionsService.save(entity);
        return success ? SaResult.ok("新增成功") : SaResult.error("新增失败");
    }

    /**
     * 更新机构
     */
    @SaCheckPermission("institutions:update")
    @PutMapping
    @Operation(summary = "更新机构")
    public SaResult updateInstitutions(@Valid @RequestBody InstitutionsUpdateDTO dto) {
        InstitutionsEntity entity = BeanMapper.map(dto, InstitutionsEntity.class);
        boolean success = institutionsService.updateById(entity);
        return success ? SaResult.ok("更新成功") : SaResult.error("更新失败");
    }

    /**
     * 删除机构
     */
    @SaCheckPermission("institutions:delete")
    @DeleteMapping("/{id}")
    @Operation(summary = "删除机构")
    public SaResult deleteInstitutions(@PathVariable("id") Long id) {
        boolean success = institutionsService.removeById(id);
        return success ? SaResult.ok("删除成功") : SaResult.error("删除失败");
    }

    /**
     * 批量删除机构
     */
    @SaCheckPermission("institutions:delete")
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除机构")
    public SaResult deleteInstitutionsBatch(@RequestBody java.util.List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return SaResult.error("请选择要删除的数据");
        }
        boolean success = institutionsService.removeByIds(ids);
        return success ? SaResult.ok("批量删除成功") : SaResult.error("批量删除失败");
    }
}
