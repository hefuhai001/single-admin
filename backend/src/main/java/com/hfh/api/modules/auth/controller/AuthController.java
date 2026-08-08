package com.hfh.api.modules.auth.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.hfh.api.base.BaseController;
import com.hfh.api.modules.auth.dto.RegisterDTO;
import com.hfh.api.modules.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证接口
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController extends BaseController {

    private final AuthService authService;

    /**
     * 获取个人信息接口
     *
     * @return 个人信息
     */
    @GetMapping("/profile")
    public SaResult profile() {
        return SaResult.data(getCurrentUser());
    }

    /**
     * 登录接口（双 token 模式）
     *
     * @param username 用户名或邮箱
     * @param password 密码
     * @param deviceId 设备标识（Web-UUID）
     * @return 登录成功返回 accessToken + refreshToken，失败返回错误信息
     */
    @PostMapping("/login")
    public SaResult login(@RequestParam("username") String username,
                          @RequestParam("password") String password,
                          @RequestParam(value = "deviceId", required = false) String deviceId
    ) {
        return SaResult.data(authService.login(username, password, deviceId));
    }

    /**
     * 刷新 accessToken 接口
     * 使用 refreshToken 换取新的 accessToken
     *
     * @param refreshToken refreshToken 值
     * @return 新的 accessToken
     */
    @PostMapping("/refresh")
    public SaResult refresh(@RequestParam("refreshToken") String refreshToken) {
        return SaResult.data(authService.refresh(refreshToken));
    }

    /**
     * 退出登录接口
     * 按设备退出：仅注销当前设备的 accessToken 和 refreshToken，不影响其他设备登录
     *
     * @return 退出登录成功返回成功信息
     */
    @PostMapping("/logout")
    public SaResult logout() {
        authService.logout();
        return SaResult.ok();
    }

    /**
     * 检查登录登录状态接口
     *
     * @return 登录状态信息
     */
    @GetMapping("/isLogin")
    public SaResult isLogin() {
        return SaResult.data(StpUtil.isLogin());
    }

    /**
     * 用户注册接口
     *
     * @param dto 注册信息
     * @return 注册结果
     */
    @PostMapping("/register")
    public SaResult register(@RequestBody RegisterDTO dto) {
        return SaResult.data(authService.register(dto));
    }
}
