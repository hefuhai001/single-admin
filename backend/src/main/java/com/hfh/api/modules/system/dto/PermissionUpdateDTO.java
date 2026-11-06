package com.hfh.api.modules.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PermissionUpdateDTO {

    @NotNull(message = "权限ID不能为空")
    private Long id;

    @NotBlank(message = "权限编码不能为空")
    @Size(max = 100, message = "权限编码长度不能超过 100 个字符")
    private String permissionCode;

    @NotBlank(message = "权限名称不能为空")
    @Size(max = 100, message = "权限名称长度不能超过 100 个字符")
    private String permissionName;

    private Integer permissionType;

    private Long parentId;

    @Size(max = 200, message = "路由路径长度不能超过 200 个字符")
    private String routePath;

    @Size(max = 200, message = "组件路径长度不能超过 200 个字符")
    private String componentPath;

    @Size(max = 200, message = "重定向地址长度不能超过 200 个字符")
    private String redirect;

    @Size(max = 100, message = "菜单图标长度不能超过 100 个字符")
    private String icon;

    private Integer sort;

    private Integer visible;

    private Integer status;
}
