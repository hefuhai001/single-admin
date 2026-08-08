package com.hfh.api.modules.auth.service;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hfh.api.exception.BusinessException;
import com.hfh.api.modules.auth.dto.RegisterDTO;
import com.hfh.api.modules.system.entity.ConfigEntity;
import com.hfh.api.modules.system.entity.InstitutionsEntity;
import com.hfh.api.modules.system.entity.RoleEntity;
import com.hfh.api.modules.system.entity.UserEntity;
import com.hfh.api.modules.system.entity.UserRoleEntity;
import com.hfh.api.modules.system.mapper.InstitutionsMapper;
import com.hfh.api.modules.system.mapper.RoleMapper;
import com.hfh.api.modules.system.mapper.UserMapper;
import com.hfh.api.modules.system.mapper.UserRoleMapper;
import com.hfh.api.modules.system.service.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 认证服务
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    /** refreshToken 有效期：7天（秒） */
    private static final long REFRESH_TOKEN_TIMEOUT = 7 * 24 * 60 * 60;

    /** session 中存储 refreshToken 的 key */
    private static final String REFRESH_TOKEN_SESSION_KEY = "refreshToken";

    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final InstitutionsMapper institutionsMapper;
    private final ConfigService configService;
    private final CaptchaService captchaService;

    /**
     * 用户登录（双 token 模式）
     * 支持使用用户名或邮箱登录
     *
     * @param account  用户名或邮箱
     * @param password 密码
     * @return 登录成功返回 accessToken + refreshToken，失败返回错误信息
     */
    public Map<String, Object> login(String account, String password) {
        UserEntity user;
        if (account != null && account.contains("@")) {
            user = userMapper.selectOne(
                    new QueryWrapper<UserEntity>().eq("email", account)
            );
        } else {
            user = userMapper.selectOne(
                    new QueryWrapper<UserEntity>().eq("username", account)
            );
        }
        if (user == null || !PASSWORD_ENCODER.matches(password, user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
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
        return data;
    }

    /**
     * 刷新 accessToken
     * 使用 refreshToken 换取新的 accessToken
     *
     * @param refreshToken refreshToken 值
     * @return 新的 accessToken + refreshToken
     */
    public Map<String, Object> refresh(String refreshToken) {
        // 校验 refreshToken 是否有效
        Object loginId = StpUtil.getLoginIdByToken(refreshToken);
        if (loginId == null) {
            throw new BusinessException("refreshToken 已失效，请重新登录");
        }

        // 校验该 token 是否为 session 中登记的 refreshToken，防止 accessToken 被冒用
        SaSession session = StpUtil.getSessionByLoginId(loginId, false);
        if (session == null) {
            throw new BusinessException("refreshToken 已失效，请重新登录");
        }
        String storedRefreshToken = session.getString(REFRESH_TOKEN_SESSION_KEY);
        if (!refreshToken.equals(storedRefreshToken)) {
            throw new BusinessException("无效的 refreshToken");
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
        return data;
    }

    /**
     * 用户注册
     *
     * @param dto 注册信息
     * @return 注册成功后的用户信息 + 双 token
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> register(RegisterDTO dto) {
        // 1. 校验注册功能是否开启
        ConfigEntity registerEnableConfig = configService.getConfigByKey("register.enable");
        if (registerEnableConfig == null || !"true".equalsIgnoreCase(registerEnableConfig.getConfigValue())) {
            throw new BusinessException("注册功能已关闭");
        }

        // 2. 校验两次密码是否一致
        if (!Objects.equals(dto.getPassword(), dto.getConfirmPassword())) {
            throw new BusinessException("两次输入的密码不一致");
        }

        // 3. 校验用户名是否已存在
        UserEntity existUser = userMapper.selectOne(
                new QueryWrapper<UserEntity>().eq("username", dto.getUsername())
        );
        if (existUser != null) {
            throw new BusinessException("用户名已存在");
        }

        // 4. 校验邮箱是否已存在
        if (dto.getEmail() != null && !dto.getEmail().isEmpty()) {
            UserEntity existEmailUser = userMapper.selectOne(
                    new QueryWrapper<UserEntity>().eq("email", dto.getEmail())
            );
            if (existEmailUser != null) {
                throw new BusinessException("邮箱已被注册");
            }
        }

        // 5. 校验邮箱验证码
        ConfigEntity verifyCodeConfig = configService.getConfigByKey("register.verify_code.enable");
        if (verifyCodeConfig != null && "true".equalsIgnoreCase(verifyCodeConfig.getConfigValue())) {
            if (dto.getVerifyCode() == null || dto.getVerifyCode().isEmpty()) {
                throw new BusinessException("邮箱验证码不能为空");
            }
            boolean codeValid = captchaService.verifyEmailCode(dto.getEmail(), dto.getVerifyCode());
            if (!codeValid) {
                throw new BusinessException("邮箱验证码错误或已过期");
            }
        }

        // 6. 保存用户信息
        UserEntity user = new UserEntity();
        user.setUsername(dto.getUsername());
        user.setPassword(PASSWORD_ENCODER.encode(dto.getPassword()));
        user.setNickname(dto.getNickname());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setStatus(1);
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());

        // 7. 分配默认机构
        String defaultInstitutionCode = configService.getConfigValue("register.default_institution", "main");
        if (defaultInstitutionCode != null && !defaultInstitutionCode.isEmpty()) {
            InstitutionsEntity defaultInstitution = institutionsMapper.selectOne(
                    new QueryWrapper<InstitutionsEntity>().eq("institution_code", defaultInstitutionCode)
            );
            if (defaultInstitution != null) {
                user.setInstitutionId(defaultInstitution.getId());
            }
        }

        userMapper.insert(user);

        // 8. 分配默认角色
        String defaultRoleCode = configService.getConfigValue("register.default_role", "common_user");
        RoleEntity defaultRole = roleMapper.selectOne(
                new QueryWrapper<RoleEntity>().eq("role_code", defaultRoleCode)
        );
        if (defaultRole != null) {
            UserRoleEntity userRole = new UserRoleEntity();
            userRole.setUserId(user.getId());
            userRole.setRoleId(defaultRole.getId());
            userRole.setCreatedAt(new Date());
            userRole.setUpdatedAt(new Date());
            userRoleMapper.insert(userRole);
        }

        // 9. 注册成功后直接登录并生成双 token
        StpUtil.login(user.getId());
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();

        // 10. 生成 refreshToken
        StpUtil.login(user.getId(), new SaLoginModel()
                .setDevice("refresh")
                .setTimeout(REFRESH_TOKEN_TIMEOUT));
        String refreshTokenValue = StpUtil.getTokenValue();

        // 11. 将 refreshToken 存入用户 session
        SaSession session = StpUtil.getSessionByLoginId(user.getId(), true);
        session.set(REFRESH_TOKEN_SESSION_KEY, refreshTokenValue);

        // 12. 返回用户信息 + token
        Map<String, Object> data = new HashMap<>();
        data.put("user", user);
        data.put("accessToken", tokenInfo.getTokenValue());
        data.put("refreshToken", refreshTokenValue);
        return data;
    }
}
