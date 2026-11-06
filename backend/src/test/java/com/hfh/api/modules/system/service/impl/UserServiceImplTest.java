package com.hfh.api.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.hfh.api.config.PermissionCache;
import com.hfh.api.exception.BusinessException;
import com.hfh.api.modules.system.dto.UserSaveDTO;
import com.hfh.api.modules.system.dto.UserUpdateDTO;
import com.hfh.api.modules.system.entity.PermissionEntity;
import com.hfh.api.modules.system.entity.UserEntity;
import com.hfh.api.modules.system.entity.UserRoleEntity;
import com.hfh.api.modules.system.mapper.UserMapper;
import com.hfh.api.modules.system.service.UserRoleService;
import com.hfh.api.modules.system.vo.UserVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * UserServiceImpl 单元测试：不启动 Spring 上下文，Mapper 与关联服务全部 mock。
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserRoleService userRoleService;

    @Mock
    private PermissionCache permissionCache;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRoleService, permissionCache);
        // ServiceImpl 的 baseMapper 由 Spring 注入，这里通过反射替换为 mock
        ReflectionTestUtils.setField(userService, "baseMapper", userMapper);
    }

    @Test
    void createUserShouldEncryptPasswordAndSaveRoles() {
        UserSaveDTO dto = new UserSaveDTO();
        dto.setUsername("alice");
        dto.setPassword("secret123");
        dto.setNickname("Alice");
        dto.setRoleIds(List.of(10L, 11L));

        // 模拟数据库自增主键回填
        when(userMapper.insert(any(UserEntity.class))).thenAnswer(invocation -> {
            invocation.<UserEntity>getArgument(0).setId(1L);
            return 1;
        });

        userService.createUser(dto);

        ArgumentCaptor<UserEntity> entityCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userMapper).insert(entityCaptor.capture());
        UserEntity saved = entityCaptor.getValue();

        assertThat(saved.getPassword())
                .isNotEqualTo("secret123")
                .hasSize(60);
        assertThat(new BCryptPasswordEncoder().matches("secret123", saved.getPassword())).isTrue();
        verify(userRoleService).saveUserRoles(1L, List.of(10L, 11L));
    }

    @Test
    void createUserShouldNotTouchUserRoleWhenRoleIdsEmpty() {
        UserSaveDTO dto = new UserSaveDTO();
        dto.setUsername("bob");
        dto.setPassword("secret123");

        when(userMapper.insert(any(UserEntity.class))).thenReturn(1);

        userService.createUser(dto);

        verifyNoInteractions(userRoleService);
    }

    @Test
    void createUserShouldThrowWhenInsertFails() {
        UserSaveDTO dto = new UserSaveDTO();
        dto.setUsername("bob");
        dto.setPassword("secret123");

        when(userMapper.insert(any(UserEntity.class))).thenReturn(0);

        assertThatThrownBy(() -> userService.createUser(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessage("新增失败");
    }

    @Test
    void updateUserShouldKeepPasswordWhenBlank() {
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setId(1L);
        dto.setUsername("alice");
        dto.setPassword("   ");

        when(userMapper.updateById(any(UserEntity.class))).thenReturn(1);

        userService.updateUser(dto);

        ArgumentCaptor<UserEntity> entityCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userMapper).updateById(entityCaptor.capture());
        assertThat(entityCaptor.getValue().getPassword()).isNull();
        verify(userRoleService, never()).saveUserRoles(any(), any());
        verify(permissionCache).evictUser(1L);
    }

    @Test
    void updateUserShouldEncryptNewPasswordAndReplaceRoles() {
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setId(1L);
        dto.setUsername("alice");
        dto.setPassword("newSecret123");
        dto.setRoleIds(List.of(7L));

        when(userMapper.updateById(any(UserEntity.class))).thenReturn(1);

        userService.updateUser(dto);

        ArgumentCaptor<UserEntity> entityCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userMapper).updateById(entityCaptor.capture());
        assertThat(new BCryptPasswordEncoder()
                .matches("newSecret123", entityCaptor.getValue().getPassword())).isTrue();
        verify(userRoleService).saveUserRoles(1L, List.of(7L));
        verify(permissionCache).evictUser(1L);
    }

    @Test
    void deleteUserShouldRemoveRelationsThenEvictCache() {
        when(userRoleService.remove(any())).thenReturn(true);
        when(userMapper.deleteById(1L)).thenReturn(1);

        userService.deleteUser(1L);

        verify(userRoleService).remove(any());
        verify(userMapper).deleteById(1L);
        verify(permissionCache).evictUser(1L);
    }

    @Test
    void deleteUsersShouldRejectEmptyIds() {
        assertThatThrownBy(() -> userService.deleteUsers(List.of()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("请选择要删除的数据");

        verifyNoInteractions(userRoleService, userMapper, permissionCache);
    }

    @Test
    void deleteUsersShouldEvictEachUserCache() {
        when(userRoleService.remove(any())).thenReturn(true);
        when(userMapper.deleteByIds(anyCollection())).thenReturn(2);

        userService.deleteUsers(List.of(1L, 2L));

        verify(permissionCache).evictUser(1L);
        verify(permissionCache).evictUser(2L);
    }

    @Test
    void getUserDetailShouldReturnNullWhenUserMissing() {
        when(userMapper.selectById(1L)).thenReturn(null);

        assertThat(userService.getUserDetail(1L)).isNull();
        verifyNoInteractions(userRoleService);
    }

    @Test
    void getUserDetailShouldMapRoleIds() {
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setUsername("alice");
        user.setNickname("Alice");
        when(userMapper.selectById(1L)).thenReturn(user);

        UserRoleEntity role1 = new UserRoleEntity();
        role1.setRoleId(10L);
        UserRoleEntity role2 = new UserRoleEntity();
        role2.setRoleId(11L);
        when(userRoleService.list(any(Wrapper.class))).thenReturn(List.of(role1, role2));

        UserVO vo = userService.getUserDetail(1L);

        assertThat(vo).isNotNull();
        assertThat(vo.getUsername()).isEqualTo("alice");
        assertThat(vo.getRoleIds()).containsExactly(10L, 11L);
    }

    @Test
    void getUserPermissionsShouldFilterDisabledAndDeduplicate() {
        PermissionEntity enabledA = permission("user:query", 1);
        PermissionEntity duplicateA = permission("user:query", 1);
        PermissionEntity disabledB = permission("user:delete", 0);
        when(userMapper.selectUserPermissions(1L))
                .thenReturn(List.of(enabledA, duplicateA, disabledB));

        assertThat(userService.getUserPermissions(1L)).containsExactly("user:query");
    }

    private PermissionEntity permission(String code, Integer status) {
        PermissionEntity permission = new PermissionEntity();
        permission.setPermissionCode(code);
        permission.setStatus(status);
        return permission;
    }
}
