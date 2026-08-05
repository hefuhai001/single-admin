package com.hfh.api.modules.system.controller;

import com.hfh.api.modules.system.entity.DictTypeEntity;
import com.hfh.api.modules.system.service.DictTypeService;
import com.hfh.api.base.BaseController;
import cn.dev33.satoken.util.SaResult;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController(value = "dictTypeEntityController")
@RequestMapping("/dict/dict-type")
@RequiredArgsConstructor
@Tag(name = "管理", description = "增删改查接口")
public class DictTypeController extends BaseController {

    private final DictTypeService dictTypeService;

    /**
     * 根据ID查询
     */
    @SaCheckPermission("dictType:query")
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询")
    public SaResult getDictTypeById(@PathVariable("id") Long id) {
        DictTypeEntity entity = dictTypeService.getById(id);
        if (entity == null) {
            return SaResult.error("不存在");
        }
        return SaResult.data(entity);
    }

    /**
     * 查询所有
     */
    @SaCheckPermission("dictType:query")
    @GetMapping("/list")
    @Operation(summary = "查询所有")
    public SaResult listDictType() {
        QueryWrapper<DictTypeEntity> wrapper = new QueryWrapper<>();
        wrapper.orderByAsc("id");
        return SaResult.data(dictTypeService.list(wrapper));
    }

    /**
     * 分页查询
     */
    @SaCheckPermission("dictType:query")
    @GetMapping("/page")
    @Operation(summary = "分页查询")
    public SaResult pageDictType(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "") @RequestParam(required = false) Long id,
            @Parameter(description = "字典名称（如：性别）") @RequestParam(required = false) String dictName,
            @Parameter(description = "字典类型（唯一标识，如：sex）") @RequestParam(required = false) String dictType
    ) {
        Page<DictTypeEntity> page = new Page<>(current, size);
        QueryWrapper<DictTypeEntity> wrapper = new QueryWrapper<>();
        wrapper.eq(id != null, "id", id);
        wrapper.like(dictName != null && !dictName.isEmpty(), "dict_name", dictName);
        wrapper.like(dictType != null && !dictType.isEmpty(), "dict_type", dictType);
        wrapper.orderByDesc("id");
        return SaResult.data(dictTypeService.page(page, wrapper));
    }

    /**
     * 新增
     */
    @SaCheckPermission("dictType:add")
    @PostMapping
    @Operation(summary = "新增")
    public SaResult saveDictType(@RequestBody DictTypeEntity entity) {
        boolean success = dictTypeService.save(entity);
        return success ? SaResult.ok("新增成功") : SaResult.error("新增失败");
    }

    /**
     * 更新
     */
    @SaCheckPermission("dictType:update")
    @PutMapping
    @Operation(summary = "更新")
    public SaResult updateDictType(@RequestBody DictTypeEntity entity) {
        boolean success = dictTypeService.updateById(entity);
        return success ? SaResult.ok("更新成功") : SaResult.error("更新失败");
    }

    /**
     * 删除
     */
    @SaCheckPermission("dictType:delete")
    @DeleteMapping("/{id}")
    @Operation(summary = "删除")
    public SaResult deleteDictType(@PathVariable("id") Long id) {
        boolean success = dictTypeService.removeById(id);
        return success ? SaResult.ok("删除成功") : SaResult.error("删除失败");
    }

    /**
     * 批量删除
     */
    @SaCheckPermission("dictType:delete")
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除")
    public SaResult deleteDictTypeBatch(@RequestBody java.util.List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return SaResult.error("请选择要删除的数据");
        }
        boolean success = dictTypeService.removeByIds(ids);
        return success ? SaResult.ok("批量删除成功") : SaResult.error("批量删除失败");
    }
}
