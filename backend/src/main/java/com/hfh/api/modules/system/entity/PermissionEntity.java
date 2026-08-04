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
 * 权限表（1:接口权限 2:路由权限）
 * </p>
 *
 * @author xiaoHe
 * @since 2026-04-01
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("sys_permission")
public class PermissionEntity implements Serializable {


    /**
     * 权限ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 权限编码
     */
    @TableField("permission_code")
    private String permissionCode;

    /**
     * 权限名称
     */
    @TableField("permission_name")
    private String permissionName;

    /**
     * 权限类型 1-接口权限 2-路由权限
     */
    @TableField("permission_type")
    private Integer permissionType;

    /**
     * 父权限ID
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 路由路径
     */
    @TableField("route_path")
    private String routePath;

    /**
     * 组件路径
     */
    @TableField("component_path")
    private String componentPath;

    /**
     * 重定向地址
     */
    @TableField("redirect")
    private String redirect;

    /**
     * 菜单图标
     */
    @TableField("icon")
    private String icon;

    /**
     * 排序序号
     */
    @TableField("sort")
    private Integer sort;

    /**
     * 是否显示 0-隐藏 1-显示
     */
    @TableField("visible")
    private Integer visible;

    /**
     * 状态 0-禁用 1-正常
     */
    @TableField("status")
    private Integer status;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private Date createdAt;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    private Date updatedAt;

    /**
     * 子权限列表（非数据库字段）
     */
    @TableField(exist = false)
    private List<PermissionEntity> children;

}
