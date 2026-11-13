package com.hfh.api.modules.system.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hfh.api.modules.system.entity.DictDataEntity;
import com.hfh.api.modules.system.entity.DictTypeEntity;
import com.hfh.api.modules.system.mapper.DictDataMapper;
import com.hfh.api.modules.system.mapper.DictTypeMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DictCacheServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @Mock
    private DictTypeMapper dictTypeMapper;
    @Mock
    private DictDataMapper dictDataMapper;

    private DictCacheService cacheService;

    @BeforeEach
    void setUp() {
        cacheService = new DictCacheService(redisTemplate, dictTypeMapper, dictDataMapper, new ObjectMapper());
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.keys(anyString())).thenReturn(Set.of());
    }

    private DictDataEntity data(String type, String value, String label) {
        DictDataEntity entity = new DictDataEntity();
        entity.setId(1L);
        entity.setDictType(type);
        entity.setDictValue(value);
        entity.setDictLabel(label);
        return entity;
    }

    private DictTypeEntity type(String type) {
        DictTypeEntity entity = new DictTypeEntity();
        entity.setId(1L);
        entity.setDictType(type);
        return entity;
    }

    @Test
    void getAllDictTypesShouldReadCache() {
        when(valueOperations.get("dict:type:all")).thenReturn("[{\"dictType\":\"status\"}]");

        List<DictTypeEntity> list = cacheService.getAllDictTypes();

        assertThat(list).hasSize(1);
        verify(dictTypeMapper, never()).selectList(any());
    }

    @Test
    void getAllDictTypesShouldRefreshWhenMiss() {
        when(valueOperations.get("dict:type:all")).thenReturn(null, "[{\"dictType\":\"status\"}]");
        when(dictTypeMapper.selectList(any())).thenReturn(List.of(type("status")));

        List<DictTypeEntity> list = cacheService.getAllDictTypes();

        assertThat(list).hasSize(1);
        verify(valueOperations).set(eq("dict:type:all"), anyString(), eq(7L), eq(TimeUnit.DAYS));
    }

    @Test
    void getAllDictTypesShouldReturnNullOnInvalidJson() {
        when(valueOperations.get("dict:type:all")).thenReturn("bad-json");

        assertThat(cacheService.getAllDictTypes()).isNull();
    }

    @Test
    void refreshAllShouldWriteGroupedDataAndCleanDirtyKeys() {
        when(dictTypeMapper.selectList(any())).thenReturn(List.of(type("status")));
        when(dictDataMapper.selectList(any())).thenReturn(List.of(
                data("status", "1", "启用"), data("status", "0", "禁用"), data("gender", "m", "男")));
        when(redisTemplate.keys("dict:data:*"))
                .thenReturn(Set.of("dict:data:status", "dict:data:gender", "dict:data:stale"));

        cacheService.refreshAll();

        verify(valueOperations).set(eq("dict:data:status"), anyString(), eq(7L), eq(TimeUnit.DAYS));
        verify(valueOperations).set(eq("dict:data:gender"), anyString(), eq(7L), eq(TimeUnit.DAYS));
        verify(redisTemplate).delete("dict:data:stale");
        verify(redisTemplate, never()).delete("dict:data:status");
    }

    @Test
    void refreshAllShouldSwallowFailure() {
        when(dictTypeMapper.selectList(any())).thenThrow(new RuntimeException("db down"));

        cacheService.refreshAll();
    }

    @Test
    void getDictDataListShouldRejectBlankType() {
        assertThat(cacheService.getDictDataList(null)).isEmpty();
        assertThat(cacheService.getDictDataList("")).isEmpty();
    }

    @Test
    void getDictDataListShouldReadCacheAndRefreshOnMiss() {
        when(valueOperations.get("dict:data:status"))
                .thenReturn("[{\"dictType\":\"status\",\"dictValue\":\"1\",\"dictLabel\":\"启用\"}]");

        assertThat(cacheService.getDictDataList("status")).hasSize(1);

        when(valueOperations.get("dict:data:gender")).thenReturn(null, "[]");
        when(dictDataMapper.selectList(any())).thenReturn(List.of(data("gender", "m", "男")));

        assertThat(cacheService.getDictDataList("gender")).isEmpty();
    }

    @Test
    void getDictLabelShouldMatchValue() {
        when(valueOperations.get("dict:data:status"))
                .thenReturn("[{\"dictType\":\"status\",\"dictValue\":\"1\",\"dictLabel\":\"启用\"}]");

        when(valueOperations.get("dict:data:unknown")).thenReturn("[]");
        assertThat(cacheService.getDictLabel("status", "1")).isEqualTo("启用");
        assertThat(cacheService.getDictLabel("status", "9")).isNull();
        assertThat(cacheService.getDictLabel("status", null)).isNull();
        assertThat(cacheService.getDictLabel("unknown", "1")).isNull();
    }

    @Test
    void serializeFailureShouldPropagateOnDemandRefresh() {
        ObjectMapper broken = mock(ObjectMapper.class);
        try {
            when(broken.writeValueAsString(any())).thenThrow(new RuntimeException("boom"));
        } catch (Exception ignored) {
        }
        DictCacheService service = new DictCacheService(redisTemplate, dictTypeMapper, dictDataMapper, broken);
        when(valueOperations.get("dict:data:status")).thenReturn(null);
        when(dictDataMapper.selectList(any())).thenReturn(List.of(data("status", "1", "启用")));

        assertThatThrownBy(() -> service.getDictDataList("status"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("序列化失败");
    }
}
