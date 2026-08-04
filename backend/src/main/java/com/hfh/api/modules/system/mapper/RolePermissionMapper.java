package com.hfh.api.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hfh.api.modules.system.entity.RolePermissionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 角色权限关联表 Mapper 接口
 * </p>
 *
 * @author xiaoHe
 * @since 2026-04-01
 */
@Mapper
public interface RolePermissionMapper extends BaseMapper<RolePermissionEntity> {

    @Select("SELECT DISTINCT p.permission_code FROM sys_permission p " +
            "JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "JOIN sys_user_role ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId}")
    List<String> listPermissionByUserId(@Param("userId") Long userId);
}
