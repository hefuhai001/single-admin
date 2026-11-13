package com.hfh.api.modules.system.service.impl;

import com.hfh.api.config.PermissionCache;
import com.hfh.api.modules.system.entity.DictDataEntity;
import com.hfh.api.modules.system.entity.RolePermissionEntity;
import com.hfh.api.modules.system.mapper.DictDataMapper;
import com.hfh.api.modules.system.mapper.RolePermissionMapper;
import com.hfh.api.modules.system.service.DictDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RelationAndDictServiceTest {

    @Mock
    private PermissionCache permissionCache;
    @Mock
    private RolePermissionMapper rolePermissionMapper;
    @Mock
    private DictCacheServiceHolder holder;

    /**
     * 占位接口仅用于避免与真实 DictCacheService 交互，实际测试直接 mock 该类。
     */
    interface DictCacheServiceHolder {
    }

    @Test
    void saveUserRolesShouldDeleteThenInsertAndEvictUser() {
        UserRoleServiceImpl service = new UserRoleServiceImpl(permissionCache);
        com.hfh.api.modules.system.mapper.UserRoleMapper mapper =
                mock(com.hfh.api.modules.system.mapper.UserRoleMapper.class);
        ReflectionTestUtils.setField(service, "baseMapper", mapper);

        service.saveUserRoles(7L, List.of(1L, 2L));

        verify(mapper).delete(any());
        verify(mapper, org.mockito.Mockito.times(2)).insert(any(com.hfh.api.modules.system.entity.UserRoleEntity.class));
        verify(permissionCache).evictUser(7L);
    }

    @Test
    void saveUserRolesShouldSkipInsertWhenRoleIdsEmpty() {
        UserRoleServiceImpl service = new UserRoleServiceImpl(permissionCache);
        com.hfh.api.modules.system.mapper.UserRoleMapper mapper =
                mock(com.hfh.api.modules.system.mapper.UserRoleMapper.class);
        ReflectionTestUtils.setField(service, "baseMapper", mapper);

        service.saveUserRoles(7L, null);
        service.saveUserRoles(7L, List.of());

        verify(mapper, never()).insert(any(com.hfh.api.modules.system.entity.UserRoleEntity.class));
        verify(permissionCache, org.mockito.Mockito.times(2)).evictUser(7L);
    }

    @Test
    void saveRolePermissionsShouldReplaceAndEvictAll() {
        RolePermissionServiceImpl service = spy(new RolePermissionServiceImpl(permissionCache));
        ReflectionTestUtils.setField(service, "baseMapper", rolePermissionMapper);
        doReturn(true).when(service).saveBatch(anyList());

        service.saveRolePermissions(3L, List.of(10L, 11L));

        ArgumentCaptor<Collection<RolePermissionEntity>> captor = ArgumentCaptor.forClass(Collection.class);
        verify(service).saveBatch(captor.capture());
        assertThat(captor.getValue()).extracting(RolePermissionEntity::getPermissionId)
                .containsExactly(10L, 11L);
        assertThat(captor.getValue()).extracting(RolePermissionEntity::getRoleId)
                .containsOnly(3L);
        verify(permissionCache).evictAll();
    }

    @Test
    void saveRolePermissionsShouldOnlyEvictWhenIdsEmpty() {
        RolePermissionServiceImpl service = spy(new RolePermissionServiceImpl(permissionCache));
        ReflectionTestUtils.setField(service, "baseMapper", rolePermissionMapper);

        service.saveRolePermissions(3L, List.of());

        verify(service, never()).saveBatch(any(Collection.class));
        verify(permissionCache).evictAll();
    }

    @Test
    void dictDataServiceShouldDelegateToCache() {
        com.hfh.api.modules.system.cache.DictCacheService cacheService =
                mock(com.hfh.api.modules.system.cache.DictCacheService.class);
        DictDataEntity entity = new DictDataEntity().setDictType("status").setDictValue("1");
        when(cacheService.getDictDataList("status")).thenReturn(List.of(entity));
        DictDataService service = new DictDataServiceImpl(cacheService);

        assertThat(service.listByDictType("status")).hasSize(1);
    }

    @Test
    void dictTypeServiceShouldBeInstantiable() {
        assertThat(new DictTypeServiceImpl()).isNotNull();
    }
}
