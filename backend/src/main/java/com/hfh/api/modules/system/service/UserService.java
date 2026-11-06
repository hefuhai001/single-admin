package com.hfh.api.modules.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hfh.api.modules.system.dto.UserQueryDTO;
import com.hfh.api.modules.system.dto.UserSaveDTO;
import com.hfh.api.modules.system.dto.UserUpdateDTO;
import com.hfh.api.modules.system.entity.UserEntity;
import com.hfh.api.modules.system.vo.RouteVO;
import com.hfh.api.modules.system.vo.UserVO;

import java.util.List;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author xiaoHe
 * @since 2026-04-01
 */
public interface UserService extends IService<UserEntity> {

    List<RouteVO> getUserRoutes(Long userId);

    List<String> getUserPermissions(Long userId);

    /**
     * 分页查询用户
     */
    Page<UserEntity> pageUsers(UserQueryDTO query);

    /**
     * 查询用户详情（含角色ID），不存在时返回 null
     */
    UserVO getUserDetail(Long id);

    /**
     * 新增用户（密码加密存储，并保存角色关联）
     */
    void createUser(UserSaveDTO dto);

    /**
     * 更新用户（密码为空时不修改，并覆盖角色关联）
     */
    void updateUser(UserUpdateDTO dto);

    /**
     * 删除单个用户及其角色关联
     */
    void deleteUser(Long id);

    /**
     * 批量删除用户及其角色关联
     */
    void deleteUsers(List<Long> ids);
}
