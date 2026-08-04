package com.hfh.api.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hfh.api.modules.system.entity.RolePermissionEntity;
import com.hfh.api.modules.system.mapper.RolePermissionMapper;
import com.hfh.api.modules.system.service.RolePermissionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 角色权限关联表 服务实现类
 * </p>
 *
 * @author xiaoHe
 * @since 2026-04-01
 */
@Service
public class RolePermissionServiceImpl extends ServiceImpl<RolePermissionMapper, RolePermissionEntity> implements RolePermissionService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveRolePermissions(Long roleId, List<Long> permissionIds) {
        // 先删除该角色的所有权限关联
        QueryWrapper<RolePermissionEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("role_id", roleId);
        this.remove(wrapper);
        
        // 如果权限ID列表不为空，则重新添加
        if (permissionIds != null && !permissionIds.isEmpty()) {
            List<RolePermissionEntity> rolePermissions = permissionIds.stream()
                    .map(permissionId -> {
                        RolePermissionEntity entity = new RolePermissionEntity();
                        entity.setRoleId(roleId);
                        entity.setPermissionId(permissionId);
                        return entity;
                    })
                    .collect(Collectors.toList());
            this.saveBatch(rolePermissions);
        }
    }
}
