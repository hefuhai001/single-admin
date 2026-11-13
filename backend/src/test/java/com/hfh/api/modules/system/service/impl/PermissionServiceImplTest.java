package com.hfh.api.modules.system.service.impl;

import com.hfh.api.modules.system.entity.PermissionEntity;
import com.hfh.api.modules.system.mapper.PermissionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PermissionServiceImplTest {

    @Mock
    private PermissionMapper permissionMapper;

    private PermissionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PermissionServiceImpl();
        ReflectionTestUtils.setField(service, "baseMapper", permissionMapper);
    }

    private PermissionEntity permission(Long id, Long parentId, Integer sort) {
        return new PermissionEntity().setId(id).setParentId(parentId).setSort(sort)
                .setPermissionCode("p" + id).setStatus(1);
    }

    @Test
    void listTreeShouldReturnEmptyWhenNoPermission() {
        when(permissionMapper.selectList(null)).thenReturn(List.of());

        assertThat(service.listTree()).isEmpty();
    }

    @Test
    void listTreeShouldBuildSortedTreeWithOrphanAsRoot() {
        PermissionEntity root = permission(1L, 0L, 2);
        PermissionEntity child = permission(2L, 1L, 1);
        PermissionEntity grandChild = permission(3L, 2L, null);
        PermissionEntity orphan = permission(4L, 99L, 1);
        PermissionEntity nullParent = permission(5L, null, null);
        when(permissionMapper.selectList(null))
                .thenReturn(List.of(root, child, grandChild, orphan, nullParent));

        List<PermissionEntity> tree = service.listTree();

        assertThat(tree).extracting(PermissionEntity::getId)
                .containsExactly(5L, 4L, 1L);
        PermissionEntity rootNode = tree.get(2);
        assertThat(rootNode.getChildren()).hasSize(1);
        assertThat(rootNode.getChildren().get(0).getId()).isEqualTo(2L);
        assertThat(rootNode.getChildren().get(0).getChildren().get(0).getId()).isEqualTo(3L);
    }

    @Test
    void getPermissionTreeByRoleIdShouldTreatMissingParentAsRoot() {
        PermissionEntity child = permission(10L, 20L, 1);
        PermissionEntity sibling = permission(11L, 20L, 2);
        when(permissionMapper.selectRolePermissions(5L)).thenReturn(List.of(child, sibling));

        List<PermissionEntity> tree = service.getPermissionTreeByRoleId(5L);

        assertThat(tree).hasSize(2);
        assertThat(tree).extracting(PermissionEntity::getId).containsExactly(10L, 11L);
    }

    @Test
    void listTreeShouldNotSetChildrenWhenLeaf() {
        PermissionEntity only = permission(1L, 0L, 1);
        when(permissionMapper.selectList(null)).thenReturn(List.of(only));

        assertThat(service.listTree().get(0).getChildren()).isNull();
    }
}
