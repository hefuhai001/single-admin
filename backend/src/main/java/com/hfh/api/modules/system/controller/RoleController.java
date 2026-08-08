package com.hfh.api.modules.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hfh.api.base.BaseController;
import com.hfh.api.modules.system.dto.RoleSaveDTO;
import com.hfh.api.modules.system.dto.RoleUpdateDTO;
import com.hfh.api.modules.system.entity.RoleEntity;
import com.hfh.api.modules.system.entity.RolePermissionEntity;
import com.hfh.api.modules.system.entity.UserRoleEntity;
import com.hfh.api.modules.system.service.RolePermissionService;
import com.hfh.api.modules.system.service.RoleService;
import com.hfh.api.modules.system.service.UserRoleService;
import com.hfh.api.modules.system.vo.RoleVO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController(value = "roleEntityController")
@RequestMapping("/admin/role")
@RequiredArgsConstructor
@Tag(name = "角色表管理", description = "角色表增删改查接口")
public class RoleController extends BaseController {

    private final RoleService roleService;
    private final RolePermissionService rolePermissionService;
    private final UserRoleService userRoleService;
    /**
     * 查询所有角色表接口
     * 
     * @return 所有角色表列表
     */

    @SaCheckPermission("role:query")
    @GetMapping("/list")
    @Operation(summary = "查询所有角色表")
    public SaResult listRole() {
        QueryWrapper<RoleEntity> wrapper = new QueryWrapper<>();
        wrapper.orderByAsc("id");
        return SaResult.data(roleService.list(wrapper));
    }

    @SaCheckPermission("role:query")
    @GetMapping("/page")
    @Operation(summary = "分页查询角色表")
    public SaResult pageRole(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "角色ID") @RequestParam(required = false) Long id,
            @Parameter(description = "角色名称") @RequestParam(required = false) String roleName,
            @Parameter(description = "角色编码") @RequestParam(required = false) String roleCode,
            @Parameter(description = "角色描述") @RequestParam(required = false) String description,
            @Parameter(description = "状态 0-禁用 1-正常") @RequestParam(required = false) Integer status
    ) {
        Page<RoleEntity> page = new Page<>(current, size);
        QueryWrapper<RoleEntity> wrapper = new QueryWrapper<>();
        wrapper.eq(id != null, "id", id);
        wrapper.like(roleName != null && !roleName.isEmpty(), "role_name", roleName);
        wrapper.like(roleCode != null && !roleCode.isEmpty(), "role_code", roleCode);
        wrapper.like(description != null && !description.isEmpty(), "description", description);
        wrapper.eq(status != null, "status", status);
        wrapper.orderByDesc("id");
        return SaResult.data(roleService.page(page, wrapper));
    }

    @SaCheckPermission("role:query")
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询角色表")
    public SaResult getRoleById(@PathVariable("id") Long id) {
        RoleVO roleVO = roleService.getRoleWithPermissions(id);
        if (roleVO == null) {
            return SaResult.error("角色表不存在");
        }
        return SaResult.data(roleVO);
    }

    @SaCheckPermission("role:add")
    @PostMapping
    @Operation(summary = "新增角色表")
    public SaResult saveRole(@RequestBody RoleSaveDTO dto) {
        RoleEntity entity = new RoleEntity();
        entity.setRoleName(dto.getRoleName());
        entity.setRoleCode(dto.getRoleCode());
        entity.setDescription(dto.getDescription());
        entity.setStatus(dto.getStatus());

        boolean success = roleService.save(entity);
        if (success && dto.getPermissionIds() != null && !dto.getPermissionIds().isEmpty()) {
            rolePermissionService.saveRolePermissions(entity.getId(), dto.getPermissionIds());
        }
        return success ? SaResult.ok("新增成功") : SaResult.error("新增失败");
    }

    @SaCheckPermission("role:update")
    @PutMapping
    @Operation(summary = "更新角色表")
    public SaResult updateRole(@RequestBody RoleUpdateDTO dto) {
        RoleEntity entity = new RoleEntity();
        entity.setId(dto.getId());
        entity.setRoleName(dto.getRoleName());
        entity.setRoleCode(dto.getRoleCode());
        entity.setDescription(dto.getDescription());
        entity.setStatus(dto.getStatus());

        boolean success = roleService.updateById(entity);
        if (success && dto.getPermissionIds() != null) {
            rolePermissionService.saveRolePermissions(entity.getId(), dto.getPermissionIds());
        }
        return success ? SaResult.ok("更新成功") : SaResult.error("更新失败");
    }

    @SaCheckPermission("role:delete")
    @DeleteMapping("/{id}")
    @Operation(summary = "删除角色表")
    @Transactional(rollbackFor = Exception.class)
    public SaResult deleteRole(@PathVariable("id") Long id) {
        // 先删除角色权限关联数据
        QueryWrapper<RolePermissionEntity> rolePermissionWrapper = new QueryWrapper<>();
        rolePermissionWrapper.eq("role_id", id);
        rolePermissionService.remove(rolePermissionWrapper);
        
        // 再删除用户角色关联数据
        QueryWrapper<UserRoleEntity> userRoleWrapper = new QueryWrapper<>();
        userRoleWrapper.eq("role_id", id);
        userRoleService.remove(userRoleWrapper);
        
        // 最后删除角色数据
        boolean success = roleService.removeById(id);
        return success ? SaResult.ok("删除成功") : SaResult.error("删除失败");
    }

    @SaCheckPermission("role:delete")
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除角色表")
    @Transactional(rollbackFor = Exception.class)
    public SaResult deleteRoleBatch(@RequestBody java.util.List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return SaResult.error("请选择要删除的数据");
        }
        
        // 先批量删除角色权限关联数据
        QueryWrapper<RolePermissionEntity> rolePermissionWrapper = new QueryWrapper<>();
        rolePermissionWrapper.in("role_id", ids);
        rolePermissionService.remove(rolePermissionWrapper);
        
        // 再批量删除用户角色关联数据
        QueryWrapper<UserRoleEntity> userRoleWrapper = new QueryWrapper<>();
        userRoleWrapper.in("role_id", ids);
        userRoleService.remove(userRoleWrapper);
        
        // 最后批量删除角色数据
        boolean success = roleService.removeByIds(ids);
        return success ? SaResult.ok("批量删除成功") : SaResult.error("批量删除失败");
    }
}
