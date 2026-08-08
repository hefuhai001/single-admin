package com.hfh.api.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hfh.api.modules.system.entity.RoleEntity;
import com.hfh.api.modules.system.vo.RoleVO;


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
}
