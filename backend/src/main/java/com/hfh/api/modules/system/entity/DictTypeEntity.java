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
@TableName("sys_dict_type")
public class DictTypeEntity implements Serializable {


    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 字典名称（如：性别）
     */
    @TableField("dict_name")
    private String dictName;

    /**
     * 字典类型（唯一标识，如：sex）
     */
    @TableField("dict_type")
    private String dictType;

    /**
     * 状态（0停用 1启用）
     */
    @TableField("status")
    private Boolean status;
}
