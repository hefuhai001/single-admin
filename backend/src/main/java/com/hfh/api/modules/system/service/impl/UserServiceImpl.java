package com.hfh.api.modules.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hfh.api.modules.system.entity.PermissionEntity;
import com.hfh.api.modules.system.entity.UserEntity;
import com.hfh.api.modules.system.entity.vo.RouteVO;
import com.hfh.api.modules.system.mapper.UserMapper;
import com.hfh.api.modules.system.service.UserService;
import org.springframework.stereotype.Service;

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
public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserService {

    private static final Integer PERMISSION_TYPE_ROUTE = 2;
    private static final Integer STATUS_ENABLED = 1;
    private static final Integer VISIBLE_YES = 1;

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
