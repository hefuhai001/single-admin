package com.hfh.api.modules.system.service.impl;

import com.hfh.api.modules.system.entity.PermissionEntity;
import com.hfh.api.modules.system.entity.RoleEntity;
import com.hfh.api.modules.system.mapper.RoleMapper;
import com.hfh.api.modules.system.service.PermissionService;
import com.hfh.api.modules.system.vo.RoleVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * RoleServiceImpl 单元测试。
 */
@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock
    private RoleMapper roleMapper;

    @Mock
    private PermissionService permissionService;

    private RoleServiceImpl roleService;

    @BeforeEach
    void setUp() {
        roleService = new RoleServiceImpl(permissionService);
        ReflectionTestUtils.setField(roleService, "baseMapper", roleMapper);
    }

    @Test
    void getRoleWithPermissionsShouldReturnNullWhenRoleMissing() {
        when(roleMapper.selectById(1L)).thenReturn(null);

        assertThat(roleService.getRoleWithPermissions(1L)).isNull();
        verifyNoInteractions(permissionService);
    }

    @Test
    void getRoleWithPermissionsShouldFillRoleAndPermissions() {
        RoleEntity role = new RoleEntity();
        role.setId(1L);
        role.setRoleName("管理员");
        role.setRoleCode("admin");
        role.setDescription("系统管理员");
        role.setStatus(1);
        when(roleMapper.selectById(1L)).thenReturn(role);

        PermissionEntity permission = new PermissionEntity();
        permission.setPermissionCode("user:query");
        when(permissionService.getPermissionTreeByRoleId(1L)).thenReturn(List.of(permission));

        RoleVO vo = roleService.getRoleWithPermissions(1L);

        assertThat(vo).isNotNull();
        assertThat(vo.getRoleCode()).isEqualTo("admin");
        assertThat(vo.getStatus()).isEqualTo(1);
        assertThat(vo.getPermissions()).containsExactly(permission);
        verify(permissionService).getPermissionTreeByRoleId(1L);
    }
}
