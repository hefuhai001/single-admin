package com.hfh.api.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hfh.api.modules.system.entity.RolePermissionEntity;

import java.util.List;

/**
 * <p>
 * 角色权限关联表 服务类
 * </p>
 *
 * @author xiaoHe
 * @since 2026-04-01
 */
public interface RolePermissionService extends IService<RolePermissionEntity> {

    void saveRolePermissions(Long roleId, List<Long> permissionIds);
}
