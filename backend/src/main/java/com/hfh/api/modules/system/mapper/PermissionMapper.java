package com.hfh.api.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hfh.api.modules.system.entity.PermissionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 权限表（1:接口权限 2:路由权限） Mapper 接口
 * </p>
 *
 * @author xiaoHe
 * @since 2026-04-01
 */
@Mapper
public interface PermissionMapper extends BaseMapper<PermissionEntity> {

    @Select("SELECT DISTINCT p.* FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "WHERE rp.role_id = #{roleId} AND p.status = 1 " +
            "ORDER BY p.sort ASC")
    List<PermissionEntity> selectRolePermissions(@Param("roleId") Long roleId);
}
