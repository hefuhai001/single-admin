package com.hfh.api.modules.system.vo;

import com.hfh.api.modules.system.entity.PermissionEntity;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class RoleVO {
    
    private Long id;
    
    private String roleName;
    
    private String roleCode;
    
    private String description;
    
    private Integer status;
    
    private Date createdAt;
    
    private Date updatedAt;
    
    private List<PermissionEntity> permissions;
}