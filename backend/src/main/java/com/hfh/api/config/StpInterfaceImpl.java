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

    /* 获取权限码列表 */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        Long userId = Long.valueOf(loginId.toString());
        return rolePermissionMapper.listPermissionByUserId(userId);
    }

    /* 获取角色列表 */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        Long userId = Long.valueOf(loginId.toString());
        return userRoleMapper.listRoleByUserId(userId);
    }

}