package com.hfh.api.modules.system.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserSaveDTO {

    private String username;

    private String password;

    private String nickname;

    private String email;

    private String phone;

    private Integer status;

    private List<Long> roleIds;
}
