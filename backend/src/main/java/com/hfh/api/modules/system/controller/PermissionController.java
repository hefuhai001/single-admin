package com.hfh.api.modules.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hfh.api.base.BaseController;
import com.hfh.api.modules.system.entity.PermissionEntity;
import com.hfh.api.modules.system.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController(value = "permissionEntityController")
@RequestMapping("/admin/permission")
@RequiredArgsConstructor
@Tag(name = "权限表（1:接口权限 2:路由权限）管理", description = "权限表（1:接口权限 2:路由权限）增删改查接口")
public class PermissionController extends BaseController {

    private final PermissionService permissionService;

    @SaCheckPermission("permission:query")
    @GetMapping("/list-tree")
    @Operation(summary = "查询权限树")
    public SaResult treePermission() {
        return SaResult.data(permissionService.listTree());
    }

    @SaCheckPermission("permission:query")
    @GetMapping("/page")
    @Operation(summary = "分页查询权限表（1:接口权限 2:路由权限）")
    public SaResult pagePermission(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "权限ID") @RequestParam(required = false) Long id,
            @Parameter(description = "权限编码") @RequestParam(required = false) String permissionCode,
            @Parameter(description = "权限名称") @RequestParam(required = false) String permissionName,
            @Parameter(description = "权限类型 1-接口权限 2-路由权限") @RequestParam(required = false) Integer permissionType,
            @Parameter(description = "父权限ID") @RequestParam(required = false) Long parentId,
            @Parameter(description = "路由路径") @RequestParam(required = false) String routePath,
            @Parameter(description = "组件路径") @RequestParam(required = false) String componentPath,
            @Parameter(description = "重定向地址") @RequestParam(required = false) String redirect,
            @Parameter(description = "菜单图标") @RequestParam(required = false) String icon,
            @Parameter(description = "排序序号") @RequestParam(required = false) Integer sort,
            @Parameter(description = "是否显示 0-隐藏 1-显示") @RequestParam(required = false) Integer visible,
            @Parameter(description = "状态 0-禁用 1-正常") @RequestParam(required = false) Integer status
    ) {
        Page<PermissionEntity> page = new Page<>(current, size);
        QueryWrapper<PermissionEntity> wrapper = new QueryWrapper<>();
        wrapper.eq(id != null, "id", id);
        wrapper.like(permissionCode != null && !permissionCode.isEmpty(), "permission_code", permissionCode);
        wrapper.like(permissionName != null && !permissionName.isEmpty(), "permission_name", permissionName);
        wrapper.eq(permissionType != null, "permission_type", permissionType);
        wrapper.eq(parentId != null, "parent_id", parentId);
        wrapper.like(routePath != null && !routePath.isEmpty(), "route_path", routePath);
        wrapper.like(componentPath != null && !componentPath.isEmpty(), "component_path", componentPath);
        wrapper.like(redirect != null && !redirect.isEmpty(), "redirect", redirect);
        wrapper.like(icon != null && !icon.isEmpty(), "icon", icon);
        wrapper.eq(sort != null, "sort", sort);
        wrapper.eq(visible != null, "visible", visible);
        wrapper.eq(status != null, "status", status);
        wrapper.orderByDesc("id");
        return SaResult.data(permissionService.page(page, wrapper));
    }

    @SaCheckPermission("permission:query")
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询权限表（1:接口权限 2:路由权限）")
    public SaResult getPermissionById(@PathVariable("id") Long id) {
        PermissionEntity entity = permissionService.getById(id);
        if (entity == null) {
            return SaResult.error("权限表（1:接口权限 2:路由权限）不存在");
        }
        return SaResult.data(entity);
    }

    @SaCheckPermission("permission:add")
    @PostMapping
    @Operation(summary = "新增权限表（1:接口权限 2:路由权限）")
    public SaResult savePermission(@RequestBody PermissionEntity entity) {
        boolean success = permissionService.save(entity);
        return success ? SaResult.ok("新增成功") : SaResult.error("新增失败");
    }

    @SaCheckPermission("permission:update")
    @PutMapping
    @Operation(summary = "更新权限表（1:接口权限 2:路由权限）")
    public SaResult updatePermission(@RequestBody PermissionEntity entity) {
        boolean success = permissionService.updateById(entity);
        return success ? SaResult.ok("更新成功") : SaResult.error("更新失败");
    }

    @SaCheckPermission("permission:delete")
    @DeleteMapping("/{id}")
    @Operation(summary = "删除权限表（1:接口权限 2:路由权限）")
    public SaResult deletePermission(@PathVariable("id") Long id) {
        boolean success = permissionService.removeById(id);
        return success ? SaResult.ok("删除成功") : SaResult.error("删除失败");
    }

    @SaCheckPermission("permission:delete")
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除权限表（1:接口权限 2:路由权限）")
    public SaResult deletePermissionBatch(@RequestBody java.util.List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return SaResult.error("请选择要删除的数据");
        }
        boolean success = permissionService.removeByIds(ids);
        return success ? SaResult.ok("批量删除成功") : SaResult.error("批量删除失败");
    }
}
