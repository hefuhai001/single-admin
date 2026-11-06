package com.hfh.api.modules.system.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UserUpdateDTO {

    @NotNull(message = "用户ID不能为空")
    private Long id;

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 32, message = "用户名长度必须在 3 到 32 个字符之间")
    private String username;

    /**
     * 为空表示不修改密码
     */
    @Pattern(regexp = "^$|^.{6,64}$", message = "密码长度必须在 6 到 64 个字符之间")
    private String password;

    @Size(max = 32, message = "昵称长度不能超过 32 个字符")
    private String nickname;

    @Email(message = "邮箱格式不正确")
    private String email;

    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    private Integer status;

    private List<Long> roleIds;
}
