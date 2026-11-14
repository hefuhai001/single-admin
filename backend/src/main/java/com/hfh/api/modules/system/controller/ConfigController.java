package com.hfh.api.modules.system.controller;

import com.hfh.api.modules.system.dto.ConfigSaveDTO;
import com.hfh.api.modules.system.dto.ConfigUpdateDTO;
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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.hfh.api.utils.BeanMapper;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController(value = "configEntityController")
@RequestMapping("/system/config")
@RequiredArgsConstructor
@Tag(name = "系统配置表管理", description = "系统配置表增删改查接口")
public class ConfigController extends BaseController {

    /**
     * 允许公开访问的配置键：仅包含前端渲染必需、且不含任何敏感信息的项。
     * 邮箱授权码、OSS 密钥等配置不会出现在公开接口的返回结果中。
     */
    private static final Set<String> PUBLIC_CONFIG_KEYS = Set.of(
            "site.title",
            "site.icp",
            "site.primary_color",
            "site.use_backend_theme_color",
            "register.enable",
            "register.verify_code.enable"
    );

    /**
     * 敏感配置的脱敏值。查询接口不返回明文，更新时若提交该值表示保持原值不变。
     */
    private static final String MASKED_VALUE = "******";

    private final ConfigService configService;

    /**
     * 判断配置键是否为敏感项（密钥、密码、令牌等）
     */
    private static boolean isSensitiveKey(String configKey) {
        if (configKey == null) {
            return false;
        }
        String key = configKey.toLowerCase();
        return key.contains("password") || key.contains("secret") || key.contains("accesskey")
                || key.contains("access_key") || key.contains("token");
    }

    /**
     * 敏感配置项返回脱敏值
     */
    private static void maskIfSensitive(ConfigEntity entity) {
        if (entity != null && isSensitiveKey(entity.getConfigKey())) {
            entity.setConfigValue(MASKED_VALUE);
        }
    }

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
        maskIfSensitive(entity);
        return SaResult.data(entity);
    }

    /**
     * 查询公开的系统配置（免登录接口，仅供前端渲染使用）
     * <p>只返回 {@link #PUBLIC_CONFIG_KEYS} 白名单内的配置项。</p>
     */
    @GetMapping("/list")
    @Operation(summary = "查询公开的系统配置")
    public SaResult listConfig() {
        QueryWrapper<ConfigEntity> wrapper = new QueryWrapper<>();
        wrapper.in("config_key", PUBLIC_CONFIG_KEYS);
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
        Page<ConfigEntity> result = configService.page(page, wrapper);
        result.getRecords().forEach(ConfigController::maskIfSensitive);
        return SaResult.data(result);
    }

    /**
     * 新增系统配置表
     */
    @SaCheckPermission("config:add")
    @PostMapping
    @Operation(summary = "新增系统配置表")
    public SaResult saveConfig(@Valid @RequestBody ConfigSaveDTO dto) {
        ConfigEntity entity = BeanMapper.map(dto, ConfigEntity.class);
        boolean success = configService.save(entity);
        return success ? SaResult.ok("新增成功") : SaResult.error("新增失败");
    }

    /**
     * 更新系统配置表
     */
    @SaCheckPermission("config:update")
    @PutMapping
    @Operation(summary = "更新系统配置表")
    public SaResult updateConfig(@Valid @RequestBody ConfigUpdateDTO dto) {
        ConfigEntity entity = BeanMapper.map(dto, ConfigEntity.class);
        // 敏感项回传脱敏占位值时保持数据库中的原值不变
        if (isSensitiveKey(dto.getConfigKey()) && MASKED_VALUE.equals(dto.getConfigValue())) {
            entity.setConfigValue(null);
        }
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
