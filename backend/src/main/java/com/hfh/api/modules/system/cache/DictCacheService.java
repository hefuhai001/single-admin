package com.hfh.api.modules.system.cache;

import com.hfh.api.modules.system.entity.DictDataEntity;
import com.hfh.api.modules.system.entity.DictTypeEntity;
import com.hfh.api.modules.system.mapper.DictDataMapper;
import com.hfh.api.modules.system.mapper.DictTypeMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
 * 字典缓存服务
 * <p>
 * 使用 Redis 缓存全部字典类型和字典数据，按 dictType 分 key 存储。
 * 为避免循环依赖，这里直接注入 Mapper 查询数据库，不依赖 Service 层。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DictCacheService {

    private static final String DICT_TYPE_KEY = "dict:type:all";
    private static final String DICT_DATA_KEY_PREFIX = "dict:data:";
    private static final long EXPIRE_DAYS = 7;

    private final StringRedisTemplate redisTemplate;
    private final DictTypeMapper dictTypeMapper;
    private final DictDataMapper dictDataMapper;
    private final ObjectMapper objectMapper;

    /**
     * 启动时加载全部字典缓存
     */
    @PostConstruct
    public void init() {
        refreshAll();
    }

    /**
     * 刷新全部字典缓存
     */
    public void refreshAll() {
        try {
            refreshDictTypeCache();
            refreshDictDataCache();
            log.info("字典缓存刷新完成");
        } catch (Exception e) {
            log.error("字典缓存刷新失败", e);
        }
    }

    /**
     * 刷新字典类型缓存
     */
    private void refreshDictTypeCache() {
        QueryWrapper<DictTypeEntity> wrapper = new QueryWrapper<>();
        wrapper.orderByAsc("id");
        List<DictTypeEntity> list = dictTypeMapper.selectList(wrapper);
        redisTemplate.opsForValue().set(DICT_TYPE_KEY, serialize(list), EXPIRE_DAYS, TimeUnit.DAYS);
    }

    /**
     * 刷新字典数据缓存，按 dictType 分组存储
     */
    private void refreshDictDataCache() {
        QueryWrapper<DictDataEntity> wrapper = new QueryWrapper<>();
        wrapper.orderByAsc("dict_sort");
        List<DictDataEntity> allData = dictDataMapper.selectList(wrapper);

        Map<String, List<DictDataEntity>> grouped = allData.stream()
                .collect(Collectors.groupingBy(DictDataEntity::getDictType));

        for (Map.Entry<String, List<DictDataEntity>> entry : grouped.entrySet()) {
            redisTemplate.opsForValue().set(
                    DICT_DATA_KEY_PREFIX + entry.getKey(),
                    serialize(entry.getValue()),
                    EXPIRE_DAYS,
                    TimeUnit.DAYS
            );
        }

        // 清理已不存在的类型缓存，避免残留脏数据
        String pattern = DICT_DATA_KEY_PREFIX + "*";
        for (String key : redisTemplate.keys(pattern)) {
            String type = key.substring(DICT_DATA_KEY_PREFIX.length());
            if (!grouped.containsKey(type)) {
                redisTemplate.delete(key);
            }
        }
    }

    /**
     * 获取全部字典类型
     */
    public List<DictTypeEntity> getAllDictTypes() {
        String json = redisTemplate.opsForValue().get(DICT_TYPE_KEY);
        if (json == null) {
            return refreshAndGetAllDictTypes();
        }
        return deserialize(json, new TypeReference<List<DictTypeEntity>>() {
        });
    }

    private List<DictTypeEntity> refreshAndGetAllDictTypes() {
        refreshDictTypeCache();
        String json = redisTemplate.opsForValue().get(DICT_TYPE_KEY);
        return deserialize(json, new TypeReference<List<DictTypeEntity>>() {
        });
    }

    /**
     * 根据字典类型获取字典数据列表
     */
    public List<DictDataEntity> getDictDataList(String dictType) {
        if (dictType == null || dictType.isEmpty()) {
            return List.of();
        }
        String json = redisTemplate.opsForValue().get(DICT_DATA_KEY_PREFIX + dictType);
        if (json == null) {
            return refreshAndGetDictDataList(dictType);
        }
        return deserialize(json, new TypeReference<List<DictDataEntity>>() {
        });
    }

    private List<DictDataEntity> refreshAndGetDictDataList(String dictType) {
        refreshDictDataCache();
        String json = redisTemplate.opsForValue().get(DICT_DATA_KEY_PREFIX + dictType);
        return deserialize(json, new TypeReference<List<DictDataEntity>>() {
        });
    }

    /**
     * 根据字典类型和字典值获取字典标签
     */
    public String getDictLabel(String dictType, String dictValue) {
        List<DictDataEntity> list = getDictDataList(dictType);
        for (DictDataEntity item : list) {
            if (dictValue != null && dictValue.equals(item.getDictValue())) {
                return item.getDictLabel();
            }
        }
        return null;
    }

    private String serialize(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("字典序列化失败", e);
        }
    }

    private <T> T deserialize(String json, TypeReference<T> typeReference) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (Exception e) {
            log.error("字典反序列化失败", e);
            return null;
        }
    }
}
