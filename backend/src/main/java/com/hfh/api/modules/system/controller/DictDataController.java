package com.hfh.api.modules.system.controller;

import com.hfh.api.modules.system.annotation.RefreshDictCache;
import com.hfh.api.modules.system.dto.DictDataSaveDTO;
import com.hfh.api.modules.system.dto.DictDataUpdateDTO;
import com.hfh.api.modules.system.entity.DictDataEntity;
import com.hfh.api.modules.system.service.DictDataService;
import com.hfh.api.base.BaseController;
import cn.dev33.satoken.util.SaResult;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.hfh.api.utils.BeanMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字典数据 Controller
 * <p>
 * 查询接口返回值中的对象字段只要标注了 {@link com.hfh.api.modules.system.annotation.DictMapping}，
 * 会由 {@link com.hfh.api.modules.system.aspect.DictMappingAspect} 自动进行字典映射翻译。
 * 增删改接口标注了 {@link RefreshDictCache}，执行成功后会自动刷新 Redis 字典缓存。
 * </p>
 */
@RestController(value = "dictDataEntityController")
@RequestMapping("/dict/dict-data")
@RequiredArgsConstructor
@Tag(name = "管理", description = "增删改查接口")
public class DictDataController extends BaseController {

    private final DictDataService dictDataService;

    /**
     * 根据ID查询
     */
    @SaCheckPermission("dictData:query")
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询")
    public SaResult getDictDataById(@PathVariable("id") Long id) {
        DictDataEntity entity = dictDataService.getById(id);
        if (entity == null) {
            return SaResult.error("不存在");
        }
        return SaResult.data(entity);
    }

    /**
     * 查询所有
     */
    @SaCheckPermission("dictData:query")
    @GetMapping("/list")
    @Operation(summary = "查询所有")
    public SaResult listDictData() {
        QueryWrapper<DictDataEntity> wrapper = new QueryWrapper<>();
        wrapper.orderByAsc("id");
        return SaResult.data(dictDataService.list(wrapper));
    }

    /**
     * 根据字典类型从 Redis 缓存查询
     */
    @SaCheckPermission("dictData:query")
    @GetMapping("/cache/{dictType}")
    @Operation(summary = "根据字典类型从缓存查询")
    public SaResult listDictDataByType(@PathVariable("dictType") String dictType) {
        List<DictDataEntity> list = dictDataService.listByDictType(dictType);
        return SaResult.data(list);
    }

    /**
     * 分页查询
     */
    @SaCheckPermission("dictData:query")
    @GetMapping("/page")
    @Operation(summary = "分页查询")
    public SaResult pageDictData(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "") @RequestParam(required = false) Long id,
            @Parameter(description = "所属字典类型（关联dict_type）") @RequestParam(required = false) String dictType,
            @Parameter(description = "显示标签（如：男、女）") @RequestParam(required = false) String dictLabel,
            @Parameter(description = "存储值（如：1、0）") @RequestParam(required = false) String dictValue,
            @Parameter(description = "排序") @RequestParam(required = false) Integer dictSort,
            @Parameter(description = "样式属性（前端用）") @RequestParam(required = false) String cssClass,
            @Parameter(description = "表格列表样式") @RequestParam(required = false) String listClass
    ) {
        Page<DictDataEntity> page = new Page<>(current, size);
        QueryWrapper<DictDataEntity> wrapper = new QueryWrapper<>();
        wrapper.eq(id != null, "id", id);
        wrapper.like(dictType != null && !dictType.isEmpty(), "dict_type", dictType);
        wrapper.like(dictLabel != null && !dictLabel.isEmpty(), "dict_label", dictLabel);
        wrapper.like(dictValue != null && !dictValue.isEmpty(), "dict_value", dictValue);
        wrapper.eq(dictSort != null, "dict_sort", dictSort);
        wrapper.like(cssClass != null && !cssClass.isEmpty(), "css_class", cssClass);
        wrapper.like(listClass != null && !listClass.isEmpty(), "list_class", listClass);
        wrapper.orderByDesc("id");
        return SaResult.data(dictDataService.page(page, wrapper));
    }

    /**
     * 新增
     */
    @SaCheckPermission("dictData:add")
    @PostMapping
    @Operation(summary = "新增")
    @RefreshDictCache
    public SaResult saveDictData(@Valid @RequestBody DictDataSaveDTO dto) {
        DictDataEntity entity = BeanMapper.map(dto, DictDataEntity.class);
        boolean success = dictDataService.save(entity);
        return success ? SaResult.ok("新增成功") : SaResult.error("新增失败");
    }

    /**
     * 更新
     */
    @SaCheckPermission("dictData:update")
    @PutMapping
    @Operation(summary = "更新")
    @RefreshDictCache
    public SaResult updateDictData(@Valid @RequestBody DictDataUpdateDTO dto) {
        DictDataEntity entity = BeanMapper.map(dto, DictDataEntity.class);
        boolean success = dictDataService.updateById(entity);
        return success ? SaResult.ok("更新成功") : SaResult.error("更新失败");
    }

    /**
     * 删除
     */
    @SaCheckPermission("dictData:delete")
    @DeleteMapping("/{id}")
    @Operation(summary = "删除")
    @RefreshDictCache
    public SaResult deleteDictData(@PathVariable("id") Long id) {
        boolean success = dictDataService.removeById(id);
        return success ? SaResult.ok("删除成功") : SaResult.error("删除失败");
    }

    /**
     * 批量删除
     */
    @SaCheckPermission("dictData:delete")
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除")
    @RefreshDictCache
    public SaResult deleteDictDataBatch(@RequestBody java.util.List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return SaResult.error("请选择要删除的数据");
        }
        boolean success = dictDataService.removeByIds(ids);
        return success ? SaResult.ok("批量删除成功") : SaResult.error("批量删除失败");
    }
}
