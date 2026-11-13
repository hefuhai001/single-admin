package com.hfh.api.modules.system.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hfh.api.modules.system.entity.ConfigEntity;
import com.hfh.api.modules.system.mapper.ConfigMapper;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ConfigCacheServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @Mock
    private ConfigMapper configMapper;

    private ConfigCacheService cacheService;

    @BeforeEach
    void setUp() {
        cacheService = new ConfigCacheService(redisTemplate, configMapper, new ObjectMapper());
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.keys(anyString())).thenReturn(Set.of());
    }

    private ConfigEntity config(String key, String value) {
        ConfigEntity entity = new ConfigEntity();
        entity.setId(1L);
        entity.setConfigKey(key);
        entity.setConfigValue(value);
        entity.setStatus(Boolean.TRUE);
        return entity;
    }

    @Test
    void getAllConfigShouldReadFromCache() {
        when(valueOperations.get("config:all")).thenReturn("[{\"configKey\":\"a\",\"configValue\":\"1\"}]");

        List<ConfigEntity> list = cacheService.getAllConfig();

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getConfigKey()).isEqualTo("a");
        verify(configMapper, never()).selectList(any());
    }

    @Test
    void getAllConfigShouldRefreshWhenCacheMiss() {
        when(valueOperations.get("config:all")).thenReturn(null, "[{\"configKey\":\"b\"}]");
        when(configMapper.selectList(null)).thenReturn(List.of(config("b", "2")));

        List<ConfigEntity> list = cacheService.getAllConfig();

        assertThat(list).hasSize(1);
        verify(valueOperations).set(eq("config:all"), anyString(), eq(7L), eq(TimeUnit.DAYS));
        verify(valueOperations).set(eq("config:key:b"), anyString(), eq(7L), eq(TimeUnit.DAYS));
    }

    @Test
    void getAllConfigShouldRefreshWhenCacheValueBlank() {
        when(valueOperations.get("config:all")).thenReturn("", null);

        List<ConfigEntity> list = cacheService.getAllConfig();

        assertThat(list).isNull();
    }

    @Test
    void refreshShouldCleanDirtyKeys() {
        when(configMapper.selectList(null)).thenReturn(List.of(config("keep", "1")));
        when(redisTemplate.keys("config:key:*"))
                .thenReturn(Set.of("config:key:keep", "config:key:stale"));

        cacheService.refreshAll();

        verify(redisTemplate).delete("config:key:stale");
        verify(redisTemplate, never()).delete("config:key:keep");
    }

    @Test
    void refreshShouldIgnoreEmptyConfigKeys() {
        ConfigEntity empty = config("", "v");
        ConfigEntity nullKey = config(null, "v");
        when(configMapper.selectList(null)).thenReturn(List.of(empty, nullKey));

        cacheService.refreshAll();

        verify(valueOperations).set(eq("config:all"), anyString(), anyLong(), any(TimeUnit.class));
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    void refreshShouldSwallowDatabaseFailure() {
        when(configMapper.selectList(null)).thenThrow(new RuntimeException("db down"));

        cacheService.refreshAll();
    }

    @Test
    void getConfigByKeyShouldRejectBlankKey() {
        assertThat(cacheService.getConfigByKey(null)).isNull();
        assertThat(cacheService.getConfigByKey("")).isNull();
    }

    @Test
    void getConfigByKeyShouldReturnCachedEntity() {
        when(valueOperations.get("config:key:a")).thenReturn("{\"configKey\":\"a\",\"configValue\":\"9\"}");

        ConfigEntity entity = cacheService.getConfigByKey("a");

        assertThat(entity.getConfigValue()).isEqualTo("9");
    }

    @Test
    void getConfigByKeyShouldRefreshAndReturnNullOnCorruptJson() {
        when(valueOperations.get("config:key:a")).thenReturn(null, "not-json");

        assertThat(cacheService.getConfigByKey("a")).isNull();
        verify(configMapper).selectList(null);
    }

    @Test
    void getConfigValueShouldApplyDefault() {
        when(valueOperations.get("config:key:a")).thenReturn("{\"configValue\":\"v\"}");
        when(valueOperations.get("config:key:missing")).thenReturn("", null);
        when(configMapper.selectList(null)).thenReturn(List.of());

        assertThat(cacheService.getConfigValue("a")).isEqualTo("v");
        assertThat(cacheService.getConfigValue("missing", "def")).isEqualTo("def");
        assertThat(cacheService.getConfigValue("missing")).isNull();
    }

    @Test
    void serializeFailureShouldPropagateWhenRefreshingOnDemand() {
        ObjectMapper broken = mock(ObjectMapper.class);
        try {
            when(broken.writeValueAsString(any())).thenThrow(new RuntimeException("boom"));
        } catch (Exception ignored) {
        }
        ConfigCacheService service = new ConfigCacheService(redisTemplate, configMapper, broken);
        when(valueOperations.get("config:key:a")).thenReturn(null);
        when(configMapper.selectList(null)).thenReturn(List.of(config("a", "1")));

        assertThatThrownBy(() -> service.getConfigByKey("a"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("序列化失败");
    }
}
