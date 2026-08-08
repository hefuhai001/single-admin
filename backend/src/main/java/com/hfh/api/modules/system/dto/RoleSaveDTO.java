package com.hfh.api.modules.system.dto;

import lombok.Data;

import java.util.List;

@Data
public class RoleSaveDTO {

    private String roleName;

    private String roleCode;

    private String description;

    private Integer status;

    private List<Long> permissionIds;
}