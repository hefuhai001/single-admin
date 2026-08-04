package com.hfh.api.modules.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hfh.api.modules.system.entity.PermissionEntity;
import com.hfh.api.modules.system.mapper.PermissionMapper;
import com.hfh.api.modules.system.service.PermissionService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 权限表（1:接口权限 2:路由权限） 服务实现类
 * </p>
 *
 * @author xiaoHe
 * @since 2026-04-01
 */
@Service
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, PermissionEntity> implements PermissionService {

    @Override
    public List<PermissionEntity> listTree() {
        List<PermissionEntity> allPermissions = baseMapper.selectList(null);
        return buildPermissionTree(allPermissions);
    }

    @Override
    public List<PermissionEntity> getPermissionTreeByRoleId(Long roleId) {
        // 查询该角色绑定的所有权限
        List<PermissionEntity> rolePermissions = baseMapper.selectRolePermissions(roleId);

        // 构建权限树
        return buildPermissionTree(rolePermissions);
    }

    private List<PermissionEntity> buildPermissionTree(List<PermissionEntity> permissions) {
        // 收集所有权限ID，用于判断节点的父节点是否在当前列表中
        java.util.Set<Long> permissionIds = permissions.stream()
                .map(PermissionEntity::getId)
                .collect(Collectors.toSet());

        Map<Long, List<PermissionEntity>> childrenMap = new HashMap<>();
        for (PermissionEntity permission : permissions) {
            Long parentId = permission.getParentId() != null ? permission.getParentId() : 0L;
            childrenMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(permission);
        }

        // 根节点：parentId 为 null/0，或父节点不在当前权限列表中（角色只保存了子节点未保存父节点的情况）
        List<PermissionEntity> rootPermissions = permissions.stream()
                .filter(p -> p.getParentId() == null || p.getParentId() == 0
                        || !permissionIds.contains(p.getParentId()))
                .sorted((a, b) -> {
                    int sortA = a.getSort() != null ? a.getSort() : 0;
                    int sortB = b.getSort() != null ? b.getSort() : 0;
                    return Integer.compare(sortA, sortB);
                })
                .collect(Collectors.toList());

        return rootPermissions.stream()
                .map(permission -> buildTreeNode(permission, childrenMap))
                .collect(Collectors.toList());
    }

    private PermissionEntity buildTreeNode(PermissionEntity permission, Map<Long, List<PermissionEntity>> childrenMap) {
        List<PermissionEntity> children = childrenMap.get(permission.getId());
        if (children != null && !children.isEmpty()) {
            children.sort((a, b) -> {
                int sortA = a.getSort() != null ? a.getSort() : 0;
                int sortB = b.getSort() != null ? b.getSort() : 0;
                return Integer.compare(sortA, sortB);
            });
            List<PermissionEntity> childrenNodes = children.stream()
                    .map(child -> buildTreeNode(child, childrenMap))
                    .collect(Collectors.toList());
            permission.setChildren(childrenNodes);
        }
        return permission;
    }
}
