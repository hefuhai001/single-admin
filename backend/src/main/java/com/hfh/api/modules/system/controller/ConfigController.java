package com.hfh.api.modules.system.controller;

import com.hfh.api.modules.system.entity.ConfigEntity;
import com.hfh.api.modules.system.service.ConfigService;
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

@RestController(value = "configEntityController")
@RequestMapping("/system/config")
@RequiredArgsConstructor
@Tag(name = "系统配置表管理", description = "系统配置表增删改查接口")
public class ConfigController extends BaseController {

    private final ConfigService configService;

    /**
     * 根据ID查询系统配置表
     */
    @SaCheckPermission("config:query")
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询系统配置表")
    public SaResult getConfigById(@PathVariable("id") Long id) {
        ConfigEntity entity = configService.getById(id);
        if (entity == null) {
            return SaResult.error("系统配置表不存在");
        }
        return SaResult.data(entity);
    }

    /**
     * 查询所有系统配置表
     */
    @GetMapping("/list")
    @Operation(summary = "查询所有系统配置表")
    public SaResult listConfig() {
        QueryWrapper<ConfigEntity> wrapper = new QueryWrapper<>();
        wrapper.orderByAsc("id");
        return SaResult.data(configService.list(wrapper));
    }

    /**
     * 分页查询系统配置表
     */
    @SaCheckPermission("config:query")
    @GetMapping("/page")
    @Operation(summary = "分页查询系统配置表")
    public SaResult pageConfig(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "主键ID") @RequestParam(required = false) Long id,
            @Parameter(description = "配置键（唯一标识，如：register.enable）") @RequestParam(required = false) String configKey,
            @Parameter(description = "配置值（存储具体配置内容）") @RequestParam(required = false) String configValue,
            @Parameter(description = "配置名称（如：是否开启注册）") @RequestParam(required = false) String configName,
            @Parameter(description = "配置分组（system/email/sms/oss等）") @RequestParam(required = false) String configGroup,
            @Parameter(description = "配置类型（string/integer/boolean/array/json）") @RequestParam(required = false) String configType,
            @Parameter(description = "备注说明") @RequestParam(required = false) String remark
    ) {
        Page<ConfigEntity> page = new Page<>(current, size);
        QueryWrapper<ConfigEntity> wrapper = new QueryWrapper<>();
        wrapper.eq(id != null, "id", id);
        wrapper.like(configKey != null && !configKey.isEmpty(), "config_key", configKey);
        wrapper.like(configValue != null && !configValue.isEmpty(), "config_value", configValue);
        wrapper.like(configName != null && !configName.isEmpty(), "config_name", configName);
        wrapper.like(configGroup != null && !configGroup.isEmpty(), "config_group", configGroup);
        wrapper.like(configType != null && !configType.isEmpty(), "config_type", configType);
        wrapper.like(remark != null && !remark.isEmpty(), "remark", remark);
        wrapper.orderByAsc("id");
        return SaResult.data(configService.page(page, wrapper));
    }

    /**
     * 新增系统配置表
     */
    @SaCheckPermission("config:add")
    @PostMapping
    @Operation(summary = "新增系统配置表")
    public SaResult saveConfig(@RequestBody ConfigEntity entity) {
        boolean success = configService.save(entity);
        return success ? SaResult.ok("新增成功") : SaResult.error("新增失败");
    }

    /**
     * 更新系统配置表
     */
    @SaCheckPermission("config:update")
    @PutMapping
    @Operation(summary = "更新系统配置表")
    public SaResult updateConfig(@RequestBody ConfigEntity entity) {
        boolean success = configService.updateById(entity);
        return success ? SaResult.ok("更新成功") : SaResult.error("更新失败");
    }

    /**
     * 删除系统配置表
     */
    @SaCheckPermission("config:delete")
    @DeleteMapping("/{id}")
    @Operation(summary = "删除系统配置表")
    public SaResult deleteConfig(@PathVariable("id") Long id) {
        boolean success = configService.removeById(id);
        return success ? SaResult.ok("删除成功") : SaResult.error("删除失败");
    }

    /**
     * 批量删除系统配置表
     */
    @SaCheckPermission("config:delete")
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除系统配置表")
    public SaResult deleteConfigBatch(@RequestBody java.util.List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return SaResult.error("请选择要删除的数据");
        }
        boolean success = configService.deleteByIds(ids);
        return success ? SaResult.ok("批量删除成功") : SaResult.error("批量删除失败");
    }
}
