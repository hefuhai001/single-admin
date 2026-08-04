package com.hfh.api.base;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 基础实体类，包含公共字段
 * </p>
 *
 * @author xiaoHe
 * @since 2025-12-23
 */
@Getter
@Setter
@Accessors(chain = true)
@Schema(name = "BaseEntity", description = "基础实体类")
public abstract class BaseEntity<T extends BaseEntity<T>> extends Model<T> {

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Date createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private Date updatedAt;

    public static final String CREATED_AT = "created_at";
    public static final String UPDATED_AT = "updated_at";

    /**
     * 获取主键值的抽象方法，子类需要实现
     */
    public abstract Serializable pkVal();
}