package com.hfh.api.modules.system.service.impl;

import com.hfh.api.modules.system.entity.InstitutionsEntity;
import com.hfh.api.modules.system.mapper.InstitutionsMapper;
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
class InstitutionsServiceImplTest {

    @Mock
    private InstitutionsMapper institutionsMapper;

    private InstitutionsServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new InstitutionsServiceImpl();
        ReflectionTestUtils.setField(service, "baseMapper", institutionsMapper);
    }

    private InstitutionsEntity institution(Long id, Long parentId) {
        return new InstitutionsEntity().setId(id).setParentId(parentId)
                .setInstitutionName("i" + id);
    }

    @Test
    void listTreeShouldReturnEmptyWhenNoData() {
        when(institutionsMapper.selectList(null)).thenReturn(List.of());

        assertThat(service.listTree()).isEmpty();
    }

    @Test
    void listTreeShouldBuildHierarchy() {
        InstitutionsEntity root = institution(1L, 0L);
        InstitutionsEntity nullRoot = institution(2L, null);
        InstitutionsEntity child = institution(3L, 1L);
        InstitutionsEntity grandChild = institution(4L, 3L);
        InstitutionsEntity orphan = institution(5L, 99L);
        when(institutionsMapper.selectList(null))
                .thenReturn(List.of(root, nullRoot, child, grandChild, orphan));

        List<InstitutionsEntity> tree = service.listTree();

        assertThat(tree).extracting(InstitutionsEntity::getId).containsExactly(1L, 2L);
        assertThat(tree.get(0).getChildren()).hasSize(1);
        assertThat(tree.get(0).getChildren().get(0).getChildren().get(0).getId()).isEqualTo(4L);
        assertThat(tree.get(1).getChildren()).isNull();
    }

    @Test
    void getChildInstitutionIdsShouldCollectRecursively() {
        when(institutionsMapper.selectList(null)).thenReturn(List.of(
                institution(1L, 0L), institution(2L, 1L), institution(3L, 1L),
                institution(4L, 2L), institution(5L, 9L)));

        assertThat(service.getChildInstitutionIds(1L)).containsExactlyInAnyOrder(2L, 3L, 4L);
        assertThat(service.getChildInstitutionIds(4L)).isEmpty();
    }

    @Test
    void getAccessibleInstitutionIdsShouldIncludeSelfAndChildren() {
        when(institutionsMapper.selectList(null)).thenReturn(List.of(
                institution(1L, 0L), institution(2L, 1L)));

        assertThat(service.getAccessibleInstitutionIds(1L)).containsExactly(1L, 2L);
        assertThat(service.getAccessibleInstitutionIds(null)).isEmpty();
    }
}
