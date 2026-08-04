package com.hfh.api.modules.system.entity.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserUpdateDTO {

    private Long id;

    private String username;

    private String password;

    private String nickname;

    private String email;

    private String phone;

    private Integer status;

    private List<Long> roleIds;
}
