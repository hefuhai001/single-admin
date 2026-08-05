package com.hfh.api.modules.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 系统配置表
 * </p>
 *
 * @author xiaoHe
 * @since 2026-08-06
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("sys_config")
public class ConfigEntity implements Serializable {


    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 配置键（唯一标识，如：register.enable）
     */
    @TableField("config_key")
    private String configKey;

    /**
     * 配置值（存储具体配置内容）
     */
    @TableField("config_value")
    private String configValue;

    /**
     * 配置名称（如：是否开启注册）
     */
    @TableField("config_name")
    private String configName;

    /**
     * 配置分组（system/email/sms/oss等）
     */
    @TableField("config_group")
    private String configGroup;

    /**
     * 配置类型（string/integer/boolean/array/json）
     */
    @TableField("config_type")
    private String configType;

    /**
     * 是否系统内置（1-是，不可删除；0-否）
     */
    @TableField("is_system")
    private Boolean isSystem;

    /**
     * 状态（0停用 1启用）
     */
    @TableField("status")
    private Boolean status;

    /**
     * 备注说明
     */
    @TableField("remark")
    private String remark;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private Date updateTime;
}
