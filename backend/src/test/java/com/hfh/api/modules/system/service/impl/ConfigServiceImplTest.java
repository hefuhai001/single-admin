package com.hfh.api.modules.system.service.impl;

import com.hfh.api.modules.system.cache.ConfigCacheService;
import com.hfh.api.modules.system.entity.ConfigEntity;
import com.hfh.api.modules.system.mapper.ConfigMapper;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ConfigServiceImplTest {

    @Mock
    private ConfigCacheService configCacheService;
    @Mock
    private ConfigMapper configMapper;

    private ConfigServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ConfigServiceImpl(configCacheService);
        ReflectionTestUtils.setField(service, "baseMapper", configMapper);
    }

    private ConfigEntity config(String key, String value, Boolean status) {
        return new ConfigEntity().setConfigKey(key).setConfigValue(value).setStatus(status);
    }

    @Test
    void getConfigByKeyShouldFilterDisabledOrMissing() {
        when(configCacheService.getConfigByKey("missing")).thenReturn(null);
        when(configCacheService.getConfigByKey("off")).thenReturn(config("off", "1", false));
        when(configCacheService.getConfigByKey("on")).thenReturn(config("on", "1", true));

        assertThat(service.getConfigByKey("missing")).isNull();
        assertThat(service.getConfigByKey("off")).isNull();
        assertThat(service.getConfigByKey("on").getConfigValue()).isEqualTo("1");
    }

    @Test
    void getConfigValueShouldFallbackToDefault() {
        when(configCacheService.getConfigByKey("on")).thenReturn(config("on", "v", true));
        when(configCacheService.getConfigByKey("off")).thenReturn(config("off", "v", false));

        assertThat(service.getConfigValue("on")).isEqualTo("v");
        assertThat(service.getConfigValue("on", "def")).isEqualTo("v");
        assertThat(service.getConfigValue("off", "def")).isEqualTo("def");
    }

    @Test
    void getIntConfigValueShouldHandleAllBranches() {
        when(configCacheService.getConfigByKey("num")).thenReturn(config("num", "42", true));
        when(configCacheService.getConfigByKey("bad")).thenReturn(config("bad", "abc", true));
        when(configCacheService.getConfigByKey("blank")).thenReturn(config("blank", "", true));
        when(configCacheService.getConfigByKey("none")).thenReturn(null);

        assertThat(service.getIntConfigValue("num", 1)).isEqualTo(42);
        assertThat(service.getIntConfigValue("bad", 7)).isEqualTo(7);
        assertThat(service.getIntConfigValue("blank", 8)).isEqualTo(8);
        assertThat(service.getIntConfigValue("none", 9)).isEqualTo(9);
    }

    @Test
    void getBooleanConfigValueShouldHandleAllBranches() {
        when(configCacheService.getConfigByKey("yes")).thenReturn(config("yes", "TRUE", true));
        when(configCacheService.getConfigByKey("no")).thenReturn(config("no", "false", true));
        when(configCacheService.getConfigByKey("blank")).thenReturn(config("blank", "", true));
        when(configCacheService.getConfigByKey("none")).thenReturn(null);

        assertThat(service.getBooleanConfigValue("yes", false)).isTrue();
        assertThat(service.getBooleanConfigValue("no", true)).isFalse();
        assertThat(service.getBooleanConfigValue("blank", true)).isTrue();
        assertThat(service.getBooleanConfigValue("none", true)).isTrue();
    }

    @Test
    void writeOperationsShouldDelegateToBaseMapper() {
        when(configMapper.insert(any(ConfigEntity.class))).thenReturn(1);
        when(configMapper.updateById(any(ConfigEntity.class))).thenReturn(1);
        when(configMapper.deleteById(any(Long.class))).thenReturn(1);
        when(configMapper.deleteByIds(anyCollection())).thenReturn(1);
        when(configMapper.deleteBatchIds(anyCollection())).thenReturn(1);

        assertThat(service.save(config("a", "1", true))).isTrue();
        assertThat(service.updateById(config("a", "2", true))).isTrue();
        assertThat(service.removeById(1L)).isTrue();
        service.deleteByIds(List.of(1L, 2L));
    }
}
