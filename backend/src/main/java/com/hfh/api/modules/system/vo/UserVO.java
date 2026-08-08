package com.hfh.api.modules.system.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class UserVO {

    private Long id;

    private String username;

    private String password;

    private String nickname;

    private String email;

    private String phone;

    private Integer status;

    private Date createdAt;

    private Date updatedAt;

    private List<Long> roleIds;
}
