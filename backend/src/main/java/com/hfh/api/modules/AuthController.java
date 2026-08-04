package com.hfh.api.modules;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hfh.api.base.BaseController;
import com.hfh.api.modules.system.entity.UserEntity;
import com.hfh.api.modules.system.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController extends BaseController {

    private final UserMapper userMapper;

    /** refreshToken 有效期：7天（秒） */
    private static final long REFRESH_TOKEN_TIMEOUT = 7 * 24 * 60 * 60;

    /** session 中存储 refreshToken 的 key */
    private static final String REFRESH_TOKEN_SESSION_KEY = "refreshToken";

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
     * @param username 用户名
     * @param password 密码
     * @return 登录成功返回 accessToken + refreshToken，失败返回错误信息
     */
    @PostMapping("/login")
    public SaResult login(@RequestParam("username") String username,
                          @RequestParam("password") String password
    ) {
        UserEntity user = userMapper.selectOne(
                new QueryWrapper<UserEntity>().eq("username", username)
        );
        if (user == null || !user.getPassword().equals(password)) {
            return SaResult.error("用户名或密码错误");
        }
        // 登录生成 accessToken
        StpUtil.login(user.getId());
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();

        // 生成 refreshToken：复用同一 loginId，通过 SaLoginModel 设置 device 和更长有效期
        StpUtil.login(user.getId(), new SaLoginModel()
                .setDevice("refresh")
                .setTimeout(REFRESH_TOKEN_TIMEOUT));
        String refreshTokenValue = StpUtil.getTokenValue();

        // 将 refreshToken 存入用户 session，用于刷新时校验
        SaSession session = StpUtil.getSessionByLoginId(user.getId(), true);
        session.set(REFRESH_TOKEN_SESSION_KEY, refreshTokenValue);

        Map<String, Object> data = new HashMap<>();
        data.put("accessToken", tokenInfo.getTokenValue());
        data.put("refreshToken", refreshTokenValue);
        return SaResult.data(data);
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
        // 校验 refreshToken 是否有效
        Object loginId = StpUtil.getLoginIdByToken(refreshToken);
        if (loginId == null) {
            return SaResult.code(401).setMsg("refreshToken 已失效，请重新登录");
        }

        // 校验该 token 是否为 session 中登记的 refreshToken，防止 accessToken 被冒用
        SaSession session = StpUtil.getSessionByLoginId(loginId, false);
        if (session == null) {
            return SaResult.code(401).setMsg("refreshToken 已失效，请重新登录");
        }
        String storedRefreshToken = session.getString(REFRESH_TOKEN_SESSION_KEY);
        if (!refreshToken.equals(storedRefreshToken)) {
            return SaResult.error("无效的 refreshToken");
        }

        // 作废旧 refreshToken
        StpUtil.logoutByTokenValue(refreshToken);

        // 生成新的 accessToken
        StpUtil.login(loginId);
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();

        // 生成新的 refreshToken（轮换），防止 refreshToken 被重复使用
        StpUtil.login(loginId, new SaLoginModel()
                .setDevice("refresh")
                .setTimeout(REFRESH_TOKEN_TIMEOUT));
        String newRefreshToken = StpUtil.getTokenValue();

        // 更新 session 中的 refreshToken
        session.set(REFRESH_TOKEN_SESSION_KEY, newRefreshToken);

        Map<String, Object> data = new HashMap<>();
        data.put("accessToken", tokenInfo.getTokenValue());
        data.put("refreshToken", newRefreshToken);
        return SaResult.data(data);
    }

    /**
     * 退出登录接口
     *
     * @return 退出登录成功返回成功信息
     */
    @PostMapping("/logout")
    public SaResult logout() {
        StpUtil.logout();
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

}
