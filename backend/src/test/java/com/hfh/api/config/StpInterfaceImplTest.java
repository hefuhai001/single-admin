package com.hfh.api.config;

import com.hfh.api.modules.system.mapper.RolePermissionMapper;
import com.hfh.api.modules.system.mapper.UserRoleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StpInterfaceImplTest {

    @Mock
    private UserRoleMapper userRoleMapper;
    @Mock
    private RolePermissionMapper rolePermissionMapper;
    @Mock
    private PermissionCache permissionCache;

    private StpInterfaceImpl stpInterface;

    @BeforeEach
    void setUp() {
        stpInterface = new StpInterfaceImpl(userRoleMapper, rolePermissionMapper, permissionCache);
    }

    @Test
    void permissionListShouldUseCacheWhenPresent() {
        when(permissionCache.getPermissions(1L)).thenReturn(List.of("a"));

        assertThat(stpInterface.getPermissionList("1", "login")).containsExactly("a");
    }

    @Test
    void permissionListShouldLoadAndCacheOnMiss() {
        when(permissionCache.getPermissions(1L)).thenReturn(null);
        when(rolePermissionMapper.listPermissionByUserId(1L)).thenReturn(List.of("b"));

        assertThat(stpInterface.getPermissionList(1L, "login")).containsExactly("b");
        verify(permissionCache).putPermissions(1L, List.of("b"));
    }

    @Test
    void roleListShouldUseCacheWhenPresent() {
        when(permissionCache.getRoles(1L)).thenReturn(List.of("admin"));

        assertThat(stpInterface.getRoleList("1", "login")).containsExactly("admin");
    }

    @Test
    void roleListShouldLoadAndCacheOnMiss() {
        when(permissionCache.getRoles(1L)).thenReturn(null);
        when(userRoleMapper.listRoleByUserId(1L)).thenReturn(List.of("user"));

        assertThat(stpInterface.getRoleList("1", "login")).containsExactly("user");
        verify(permissionCache).putRoles(1L, List.of("user"));
    }
}
