package com.hfh.api.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hfh.api.modules.system.entity.UserRoleEntity;

import java.util.List;

/**
 * <p>
 * 用户角色关联表 服务类
 * </p>
 *
 * @author xiaoHe
 * @since 2026-04-01
 */
public interface UserRoleService extends IService<UserRoleEntity> {

    void saveUserRoles(Long userId, List<Long> roleIds);
}
