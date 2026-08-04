package com.hfh.api.modules.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 机构表(支持父子层级)
 * </p>
 *
 * @author xiaoHe
 * @since 2026-04-15
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("sys_institutions")
public class InstitutionsEntity implements Serializable {


    /**
     * 机构ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 父机构ID(上级机构)
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 机构编码
     */
    @TableField("institution_code")
    private String institutionCode;

    /**
     * 机构名称
     */
    @TableField("institution_name")
    private String institutionName;

    /**
     * 状态: 0-禁用,1-启用
     */
    @TableField("status")
    private Byte status;

    @TableField("created_at")
    private Date createdAt;

    @TableField("updated_at")
    private Date updatedAt;

    /**
     * 子机构列表（非数据库字段）
     */
    @TableField(exist = false)
    private List<InstitutionsEntity> children;

}
