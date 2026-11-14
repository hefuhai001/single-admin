package com.hfh.api.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hfh.api.config.PermissionCache;
import com.hfh.api.modules.system.entity.PermissionEntity;
import com.hfh.api.modules.system.entity.RoleEntity;
import com.hfh.api.modules.system.entity.RolePermissionEntity;
import com.hfh.api.modules.system.entity.UserRoleEntity;
import com.hfh.api.modules.system.mapper.RoleMapper;
import com.hfh.api.modules.system.service.PermissionService;
import com.hfh.api.modules.system.service.RolePermissionService;
import com.hfh.api.modules.system.service.RoleService;
import com.hfh.api.modules.system.service.UserRoleService;
import com.hfh.api.modules.system.vo.RoleVO;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * 角色表 服务实现类
 * </p>
 *
 * @author xiaoHe
 * @since 2026-04-01
 */
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, RoleEntity> implements RoleService {

    private final PermissionService permissionService;
    private final RolePermissionService rolePermissionService;
    private final UserRoleService userRoleService;
    private final PermissionCache permissionCache;

    public RoleServiceImpl(PermissionService permissionService,
                           RolePermissionService rolePermissionService,
                           UserRoleService userRoleService,
                           PermissionCache permissionCache) {
        this.permissionService = permissionService;
        this.rolePermissionService = rolePermissionService;
        this.userRoleService = userRoleService;
        this.permissionCache = permissionCache;
    }

    @Override
    public RoleVO getRoleWithPermissions(Long id) {
        RoleEntity role = this.getById(id);
        if (role == null) {
            return null;
        }

        RoleVO roleVO = new RoleVO();
        roleVO.setId(role.getId());
        roleVO.setRoleName(role.getRoleName());
        roleVO.setRoleCode(role.getRoleCode());
        roleVO.setDescription(role.getDescription());
        roleVO.setStatus(role.getStatus());
        roleVO.setCreatedAt(role.getCreatedAt());
        roleVO.setUpdatedAt(role.getUpdatedAt());

        // 查询该角色绑定的所有权限
        List<PermissionEntity> rolePermissions = permissionService.getPermissionTreeByRoleId(id);
        roleVO.setPermissions(rolePermissions);

        return roleVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRole(Long id) {
        // 先删除角色权限关联数据
        rolePermissionService.remove(new QueryWrapper<RolePermissionEntity>().eq("role_id", id));

        // 再删除用户角色关联数据
        userRoleService.remove(new QueryWrapper<UserRoleEntity>().eq("role_id", id));

        // 最后删除角色数据
        boolean success = this.removeById(id);
        if (success) {
            permissionCache.evictAll();
        }
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRoles(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }

        rolePermissionService.remove(new QueryWrapper<RolePermissionEntity>().in("role_id", ids));
        userRoleService.remove(new QueryWrapper<UserRoleEntity>().in("role_id", ids));

        boolean success = this.removeByIds(ids);
        if (success) {
            permissionCache.evictAll();
        }
        return success;
    }
}
