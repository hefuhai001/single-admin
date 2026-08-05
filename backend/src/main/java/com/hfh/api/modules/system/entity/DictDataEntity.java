package com.hfh.api.modules.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author xiaoHe
 * @since 2026-08-05
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("sys_dict_data")
public class DictDataEntity implements Serializable {


    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 所属字典类型（关联dict_type）
     */
    @TableField("dict_type")
    private String dictType;

    /**
     * 显示标签（如：男、女）
     */
    @TableField("dict_label")
    private String dictLabel;

    /**
     * 存储值（如：1、0）
     */
    @TableField("dict_value")
    private String dictValue;

    /**
     * 排序
     */
    @TableField("dict_sort")
    private Integer dictSort;

    /**
     * 状态
     */
    @TableField("status")
    private Boolean status;

    /**
     * 是否默认值
     */
    @TableField("is_default")
    private Boolean isDefault;

    /**
     * 样式属性（前端用）
     */
    @TableField("css_class")
    private String cssClass;

    /**
     * 表格列表样式
     */
    @TableField("list_class")
    private String listClass;
}
