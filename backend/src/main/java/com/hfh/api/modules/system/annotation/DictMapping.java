package com.hfh.api.modules.system.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 字典映射注解
 * 标注在字段上，表示该字段的值需要按字典类型翻译成标签
 * 翻译结果写入 labelField 指定的字段中
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DictMapping {

    /**
     * 字典类型（如：sex）
     */
    String dictType();

    /**
     * 翻译结果写入的字段名（如：sexLabel）
     */
    String labelField();
}
