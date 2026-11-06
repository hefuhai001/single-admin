package com.hfh.api.config;

import cn.dev33.satoken.stp.StpInterface;
import com.hfh.api.modules.system.mapper.RolePermissionMapper;
import com.hfh.api.modules.system.mapper.UserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final UserRoleMapper userRoleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final PermissionCache permissionCache;

    /* 获取权限码列表（优先命中缓存） */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        Long userId = Long.valueOf(loginId.toString());
        List<String> cached = permissionCache.getPermissions(userId);
        if (cached != null) {
            return cached;
        }
        List<String> permissions = rolePermissionMapper.listPermissionByUserId(userId);
        permissionCache.putPermissions(userId, permissions);
        return permissions;
    }

    /* 获取角色列表（优先命中缓存） */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        Long userId = Long.valueOf(loginId.toString());
        List<String> cached = permissionCache.getRoles(userId);
        if (cached != null) {
            return cached;
        }
        List<String> roles = userRoleMapper.listRoleByUserId(userId);
        permissionCache.putRoles(userId, roles);
        return roles;
    }

}
