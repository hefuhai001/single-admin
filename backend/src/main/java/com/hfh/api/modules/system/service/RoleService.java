package com.hfh.api.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hfh.api.modules.system.entity.RoleEntity;
import com.hfh.api.modules.system.vo.RoleVO;

import java.util.List;


/**
 * <p>
 * 角色表 服务类
 * </p>
 *
 * @author xiaoHe
 * @since 2026-04-01
 */
public interface RoleService extends IService<RoleEntity> {

    RoleVO getRoleWithPermissions(Long id);

    /**
     * 删除角色，并清理角色权限、用户角色关联数据（事务在 Service 层统一控制）
     *
     * @param id 角色 ID
     * @return 是否删除成功
     */
    boolean deleteRole(Long id);

    /**
     * 批量删除角色，并清理关联数据
     *
     * @param ids 角色 ID 集合
     * @return 是否删除成功
     */
    boolean deleteRoles(List<Long> ids);
}
