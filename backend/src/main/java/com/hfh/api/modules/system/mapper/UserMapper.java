package com.hfh.api.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hfh.api.modules.system.entity.PermissionEntity;
import com.hfh.api.modules.system.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 用户表 Mapper 接口
 * </p>
 *
 * @author xiaoHe
 * @since 2026-04-01
 */
@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {


    @Select("SELECT DISTINCT p.* FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "INNER JOIN sys_user_role ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND p.status = 1 " +
            "ORDER BY p.sort ASC")
    List<PermissionEntity> selectUserPermissions(@Param("userId") Long userId);

}
