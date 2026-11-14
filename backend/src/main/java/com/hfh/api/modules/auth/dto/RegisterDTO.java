package com.hfh.api.modules.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户注册 DTO
 */
@Data
public class RegisterDTO {

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度需在 3 到 20 个字符之间")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 30, message = "密码长度需在 6 到 30 个字符之间")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{6,30}$", message = "密码需同时包含字母和数字")
    private String password;

    /**
     * 确认密码
     */
    @NotBlank(message = "请再次输入密码")
    @Size(max = 30, message = "确认密码长度不能超过 30 个字符")
    private String confirmPassword;

    /**
     * 昵称
     */
    @Size(max = 20, message = "昵称长度不能超过 20 个字符")
    private String nickname;

    /**
     * 邮箱
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过 100 个字符")
    private String email;

    /**
     * 邮箱验证码
     */
    @Size(max = 10, message = "验证码长度不正确")
    private String verifyCode;

    /**
     * 手机号
     */
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 设备标识（Web-UUID）
     */
    @Size(max = 64, message = "设备标识长度不能超过 64 个字符")
    private String deviceId;
}
