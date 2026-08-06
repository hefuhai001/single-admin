package com.hfh.api.modules.system.cache;

import com.hfh.api.modules.system.entity.ConfigEntity;
import com.hfh.api.modules.system.mapper.ConfigMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 系统配置缓存服务
 * <p>
 * 使用 Redis 缓存全部系统配置，按 configKey 分 key 存储，同时保留一个全量列表缓存。
 * 为避免循环依赖，直接注入 Mapper 查询数据库，不依赖 Service 层。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConfigCacheService {

    private static final String CONFIG_ALL_KEY = "config:all";
    private static final String CONFIG_KEY_PREFIX = "config:key:";
    private static final long EXPIRE_DAYS = 7;

    private final StringRedisTemplate redisTemplate;
    private final ConfigMapper configMapper;
    private final ObjectMapper objectMapper;

    /**
     * 启动时加载全部配置缓存
     */
    @PostConstruct
    public void init() {
        refreshAll();
    }

    /**
     * 刷新全部配置缓存
     */
    public void refreshAll() {
        try {
            refreshConfigCache();
            log.info("系统配置缓存刷新完成");
        } catch (Exception e) {
            log.error("系统配置缓存刷新失败", e);
        }
    }

    /**
     * 刷新配置缓存，按 configKey 分 key 存储，同时写入全量列表缓存
     */
    private void refreshConfigCache() {
        List<ConfigEntity> allConfig = configMapper.selectList(null);

        redisTemplate.opsForValue().set(CONFIG_ALL_KEY, serialize(allConfig), EXPIRE_DAYS, TimeUnit.DAYS);

        Map<String, String> keyMap = allConfig.stream()
                .filter(item -> item.getConfigKey() != null && !item.getConfigKey().isEmpty())
                .collect(Collectors.toMap(
                        ConfigEntity::getConfigKey,
                        this::serialize,
                        (existing, replacement) -> replacement
                ));

        for (Map.Entry<String, String> entry : keyMap.entrySet()) {
            redisTemplate.opsForValue().set(
                    CONFIG_KEY_PREFIX + entry.getKey(),
                    entry.getValue(),
                    EXPIRE_DAYS,
                    TimeUnit.DAYS
            );
        }

        // 清理已不存在的配置键缓存，避免残留脏数据
        String pattern = CONFIG_KEY_PREFIX + "*";
        for (String key : redisTemplate.keys(pattern)) {
            String configKey = key.substring(CONFIG_KEY_PREFIX.length());
            if (!keyMap.containsKey(configKey)) {
                redisTemplate.delete(key);
            }
        }
    }

    /**
     * 获取全部配置列表
     */
    public List<ConfigEntity> getAllConfig() {
        String json = redisTemplate.opsForValue().get(CONFIG_ALL_KEY);
        if (json == null) {
            return refreshAndGetAllConfig();
        }
        return deserialize(json, new TypeReference<List<ConfigEntity>>() {
        });
    }

    private List<ConfigEntity> refreshAndGetAllConfig() {
        refreshConfigCache();
        String json = redisTemplate.opsForValue().get(CONFIG_ALL_KEY);
        return deserialize(json, new TypeReference<List<ConfigEntity>>() {
        });
    }

    /**
     * 根据 configKey 获取配置对象
     */
    public ConfigEntity getConfigByKey(String configKey) {
        if (configKey == null || configKey.isEmpty()) {
            return null;
        }
        String json = redisTemplate.opsForValue().get(CONFIG_KEY_PREFIX + configKey);
        if (json == null) {
            return refreshAndGetConfigByKey(configKey);
        }
        return deserialize(json, new TypeReference<ConfigEntity>() {
        });
    }

    private ConfigEntity refreshAndGetConfigByKey(String configKey) {
        refreshConfigCache();
        String json = redisTemplate.opsForValue().get(CONFIG_KEY_PREFIX + configKey);
        return deserialize(json, new TypeReference<ConfigEntity>() {
        });
    }

    /**
     * 根据 configKey 获取配置值
     */
    public String getConfigValue(String configKey) {
        ConfigEntity config = getConfigByKey(configKey);
        return config != null ? config.getConfigValue() : null;
    }

    /**
     * 根据 configKey 获取配置值，不存在返回默认值
     */
    public String getConfigValue(String configKey, String defaultValue) {
        String value = getConfigValue(configKey);
        return value != null ? value : defaultValue;
    }

    private String serialize(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("系统配置序列化失败", e);
        }
    }

    private <T> T deserialize(String json, TypeReference<T> typeReference) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (Exception e) {
            log.error("系统配置反序列化失败", e);
            return null;
        }
    }
}
