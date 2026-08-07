package com.hfh.api.modules.auth.dto;

import lombok.Data;

/**
 * 用户注册 DTO
 */
@Data
public class RegisterDTO {

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 确认密码
     */
    private String confirmPassword;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 邮箱验证码
     */
    private String verifyCode;

    /**
     * 手机号
     */
    private String phone;
}
