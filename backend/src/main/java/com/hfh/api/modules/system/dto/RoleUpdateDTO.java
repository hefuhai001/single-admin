package com.hfh.api.modules.system.dto;

import lombok.Data;

import java.util.List;

@Data
public class RoleUpdateDTO {

    private Long id;

    private String roleName;

    private String roleCode;

    private String description;

    private Integer status;

    private List<Long> permissionIds;
}