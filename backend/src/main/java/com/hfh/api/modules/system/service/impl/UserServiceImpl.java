package com.hfh.api.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hfh.api.config.PermissionCache;
import com.hfh.api.exception.BusinessException;
import com.hfh.api.modules.system.dto.UserQueryDTO;
import com.hfh.api.modules.system.dto.UserSaveDTO;
import com.hfh.api.modules.system.dto.UserUpdateDTO;
import com.hfh.api.modules.system.entity.PermissionEntity;
import com.hfh.api.modules.system.entity.UserEntity;
import com.hfh.api.modules.system.entity.UserRoleEntity;
import com.hfh.api.modules.system.mapper.UserMapper;
import com.hfh.api.modules.system.service.UserRoleService;
import com.hfh.api.modules.system.service.UserService;
import com.hfh.api.modules.system.vo.RouteVO;
import com.hfh.api.modules.system.vo.UserVO;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author xiaoHe
 * @since 2026-04-01
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserService {

    private static final Integer PERMISSION_TYPE_ROUTE = 2;
    private static final Integer STATUS_ENABLED = 1;
    private static final Integer VISIBLE_YES = 1;

    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    private final UserRoleService userRoleService;
    private final PermissionCache permissionCache;

    @Override
    public List<RouteVO> getUserRoutes(Long userId) {
        List<PermissionEntity> allPermissions = getUserAllPermissions(userId);

        List<PermissionEntity> routePermissions = allPermissions.stream()
                .filter(p -> PERMISSION_TYPE_ROUTE.equals(p.getPermissionType()))
                .filter(p -> STATUS_ENABLED.equals(p.getStatus()))
                .collect(Collectors.toList());

        return buildRouteTree(routePermissions, allPermissions);
    }

    @Override
    public List<String> getUserPermissions(Long userId) {
        List<PermissionEntity> allPermissions = getUserAllPermissions(userId);

        List<String> permissionCodes = allPermissions.stream()
                .filter(p -> STATUS_ENABLED.equals(p.getStatus()))
                .map(PermissionEntity::getPermissionCode)
                .distinct()
                .collect(Collectors.toList());

        return permissionCodes;
    }

    @Override
    public Page<UserEntity> pageUsers(UserQueryDTO query) {
        Page<UserEntity> page = new Page<>(query.getCurrent(), query.getSize());
        QueryWrapper<UserEntity> wrapper = new QueryWrapper<>();
        wrapper.eq(query.getId() != null, "id", query.getId());
        wrapper.like(!isEmpty(query.getUsername()), "username", query.getUsername());
        wrapper.like(!isEmpty(query.getNickname()), "nickname", query.getNickname());
        wrapper.like(!isEmpty(query.getEmail()), "email", query.getEmail());
        wrapper.like(!isEmpty(query.getPhone()), "phone", query.getPhone());
        wrapper.eq(query.getStatus() != null, "status", query.getStatus());
        if (query.getRoleId() != null) {
            // 使用占位符参数，避免拼接 SQL
            wrapper.apply("id IN (SELECT user_id FROM sys_user_role WHERE role_id = {0})", query.getRoleId());
        }
        wrapper.orderByDesc("id");
        return page(page, wrapper);
    }

    @Override
    public UserVO getUserDetail(Long id) {
        UserEntity entity = getById(id);
        if (entity == null) {
            return null;
        }

        QueryWrapper<UserRoleEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", id);
        List<Long> roleIds = userRoleService.list(wrapper).stream()
                .map(UserRoleEntity::getRoleId)
                .collect(Collectors.toList());

        UserVO vo = new UserVO();
        vo.setId(entity.getId());
        vo.setUsername(entity.getUsername());
        vo.setNickname(entity.getNickname());
        vo.setEmail(entity.getEmail());
        vo.setPhone(entity.getPhone());
        vo.setStatus(entity.getStatus());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        vo.setRoleIds(roleIds);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createUser(UserSaveDTO dto) {
        UserEntity entity = new UserEntity();
        entity.setUsername(dto.getUsername());
        // 与登录校验保持一致：密码必须加密存储
        entity.setPassword(PASSWORD_ENCODER.encode(dto.getPassword()));
        entity.setNickname(dto.getNickname());
        entity.setEmail(dto.getEmail());
        entity.setPhone(dto.getPhone());
        entity.setStatus(dto.getStatus());

        if (!save(entity)) {
            throw new BusinessException("新增失败");
        }
        if (dto.getRoleIds() != null && !dto.getRoleIds().isEmpty()) {
            userRoleService.saveUserRoles(entity.getId(), dto.getRoleIds());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(UserUpdateDTO dto) {
        UserEntity entity = new UserEntity();
        entity.setId(dto.getId());
        entity.setUsername(dto.getUsername());
        entity.setNickname(dto.getNickname());
        entity.setEmail(dto.getEmail());
        entity.setPhone(dto.getPhone());
        entity.setStatus(dto.getStatus());
        // 密码为空表示不修改
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            entity.setPassword(PASSWORD_ENCODER.encode(dto.getPassword()));
        }

        if (!updateById(entity)) {
            throw new BusinessException("更新失败");
        }
        if (dto.getRoleIds() != null) {
            userRoleService.saveUserRoles(dto.getId(), dto.getRoleIds());
        }
        permissionCache.evictUser(dto.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        QueryWrapper<UserRoleEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", id);
        userRoleService.remove(wrapper);

        if (!removeById(id)) {
            throw new BusinessException("删除失败");
        }
        permissionCache.evictUser(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUsers(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("请选择要删除的数据");
        }

        QueryWrapper<UserRoleEntity> wrapper = new QueryWrapper<>();
        wrapper.in("user_id", ids);
        userRoleService.remove(wrapper);

        if (!removeByIds(ids)) {
            throw new BusinessException("批量删除失败");
        }
        ids.forEach(permissionCache::evictUser);
    }

    private boolean isEmpty(String value) {
        return value == null || value.isEmpty();
    }

    private List<PermissionEntity> getUserAllPermissions(Long userId) {
        return baseMapper.selectUserPermissions(userId);
    }

    private List<RouteVO> buildRouteTree(List<PermissionEntity> routes, List<PermissionEntity> allPermissions) {
        Map<Long, List<PermissionEntity>> childrenMap = new HashMap<>();
        for (PermissionEntity route : routes) {
            Long parentId = route.getParentId() != null ? route.getParentId() : 0L;
            childrenMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(route);
        }

        List<PermissionEntity> rootRoutes = routes.stream()
                .filter(r -> r.getParentId() == null || r.getParentId() == 0)
                .sorted((a, b) -> {
                    int sortA = a.getSort() != null ? a.getSort() : 0;
                    int sortB = b.getSort() != null ? b.getSort() : 0;
                    return Integer.compare(sortA, sortB);
                })
                .collect(Collectors.toList());

        return rootRoutes.stream()
                .map(route -> convertToRouteVO(route, childrenMap, allPermissions))
                .collect(Collectors.toList());
    }

    private RouteVO convertToRouteVO(PermissionEntity entity, Map<Long, List<PermissionEntity>> childrenMap, List<PermissionEntity> allPermissions) {
        RouteVO routeVO = new RouteVO();
        routeVO.setId(entity.getId());
        routeVO.setPath(entity.getRoutePath());
        routeVO.setName(entity.getPermissionCode());
        routeVO.setComponent(entity.getComponentPath());
        routeVO.setRedirect(entity.getRedirect());

        RouteVO.MetaVO meta = new RouteVO.MetaVO();
        meta.setTitle(entity.getPermissionName());
        meta.setIcon(entity.getIcon());
        meta.setHidden(!VISIBLE_YES.equals(entity.getVisible()));
        meta.setKeepAlive(true);

        List<PermissionEntity> childPermissions = allPermissions.stream()
                .filter(p -> entity.getId().equals(p.getParentId()))
                .filter(p -> !PERMISSION_TYPE_ROUTE.equals(p.getPermissionType()))
                .collect(Collectors.toList());
        List<String> permissionCodes = childPermissions.stream()
                .map(PermissionEntity::getPermissionCode)
                .collect(Collectors.toList());
        meta.setPermissions(permissionCodes.isEmpty() ? null : permissionCodes);

        routeVO.setMeta(meta);

        List<PermissionEntity> children = childrenMap.get(entity.getId());
        if (children != null && !children.isEmpty()) {
            children.sort((a, b) -> {
                int sortA = a.getSort() != null ? a.getSort() : 0;
                int sortB = b.getSort() != null ? b.getSort() : 0;
                return Integer.compare(sortA, sortB);
            });
            List<RouteVO> childrenVO = children.stream()
                    .map(child -> convertToRouteVO(child, childrenMap, allPermissions))
                    .collect(Collectors.toList());
            routeVO.setChildren(childrenVO);
        }

        return routeVO;
    }
}
