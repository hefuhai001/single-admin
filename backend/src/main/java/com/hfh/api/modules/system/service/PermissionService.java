package com.hfh.api.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hfh.api.modules.system.entity.PermissionEntity;

import java.util.List;

/**
 * <p>
 * 权限表（1:接口权限 2:路由权限） 服务类
 * </p>
 *
 * @author xiaoHe
 * @since 2026-04-01
 */
public interface PermissionService extends IService<PermissionEntity> {

    List<PermissionEntity> listTree();
    
    List<PermissionEntity> getPermissionTreeByRoleId(Long roleId);
}
