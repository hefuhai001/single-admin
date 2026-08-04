package com.hfh.api.modules.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hfh.api.modules.system.entity.PermissionEntity;
import com.hfh.api.modules.system.entity.RoleEntity;
import com.hfh.api.modules.system.entity.vo.RoleVO;
import com.hfh.api.modules.system.mapper.RoleMapper;
import com.hfh.api.modules.system.service.PermissionService;
import com.hfh.api.modules.system.service.RoleService;
import org.springframework.stereotype.Service;

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

    public RoleServiceImpl(PermissionService permissionService) {
        this.permissionService = permissionService;
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
}
