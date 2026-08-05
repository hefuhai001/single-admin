package com.hfh.api.modules.system.mapping;

import cn.dev33.satoken.util.SaResult;
import com.hfh.api.modules.system.annotation.DictMapping;
import com.hfh.api.modules.system.cache.DictCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

/**
 * 字典映射辅助类
 * 扫描对象中的 @DictMapping 注解字段并自动翻译字典标签
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DictMappingHelper {

    private final DictCacheService dictCacheService;

    /**
     * 对目标对象进行字典翻译
     */
    public void map(Object target) {
        if (target == null) {
            return;
        }
        if (target instanceof SaResult result) {
            map(result.getData());
            return;
        }
        if (target instanceof Collection<?> collection) {
            for (Object item : collection) {
                map(item);
            }
            return;
        }
        if (target instanceof Map<?, ?> map) {
            for (Object value : map.values()) {
                map(value);
            }
            return;
        }
        translateObject(target);
    }

    private void translateObject(Object target) {
        if (target == null) {
            return;
        }
        Class<?> clazz = target.getClass();
        // 跳过 JDK 内置类型和基础类型
        if (clazz.isPrimitive() || clazz.getName().startsWith("java.")) {
            return;
        }

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            DictMapping mapping = field.getAnnotation(DictMapping.class);
            if (mapping != null) {
                translateField(target, field, mapping);
            } else {
                // 递归处理非基础类型字段
                if (isComplexType(field.getType())) {
                    try {
                        field.setAccessible(true);
                        Object value = field.get(target);
                        map(value);
                    } catch (IllegalAccessException e) {
                        log.warn("字典映射递归字段失败: {}", field.getName(), e);
                    }
                }
            }
        }
    }

    private void translateField(Object target, Field field, DictMapping mapping) {
        try {
            field.setAccessible(true);
            Object value = field.get(target);
            if (value == null) {
                return;
            }
            String dictValue = formatDictValue(value);
            String label = dictCacheService.getDictLabel(mapping.dictType(), dictValue);
            if (label == null) {
                return;
            }
            Field labelField = findField(target.getClass(), mapping.labelField());
            if (labelField == null) {
                log.warn("字典映射目标字段不存在: {}.{}", target.getClass().getName(), mapping.labelField());
                return;
            }
            labelField.setAccessible(true);
            labelField.set(target, label);
        } catch (IllegalAccessException e) {
            log.warn("字典映射字段失败: {}", field.getName(), e);
        }
    }

    private String formatDictValue(Object value) {
        if (value instanceof Boolean b) {
            return b ? "1" : "0";
        }
        return value.toString();
    }

    private Field findField(Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    private boolean isComplexType(Class<?> type) {
        return !type.isPrimitive()
                && !type.getName().startsWith("java.lang")
                && !type.getName().startsWith("java.math")
                && !type.getName().startsWith("java.time")
                && !type.isEnum();
    }
}
