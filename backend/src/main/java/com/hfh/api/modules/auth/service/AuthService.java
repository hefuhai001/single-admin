package com.hfh.api.modules.auth.service;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hfh.api.config.SysConfigKeys;
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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.time.Duration;

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

    /** 登录失败次数 Redis key 前缀 */
    private static final String LOGIN_FAIL_COUNT_KEY = SysConfigKeys.REDIS_LOGIN_FAIL_PREFIX;

    /** 账号锁定 Redis key 前缀 */
    private static final String LOGIN_LOCK_KEY = SysConfigKeys.REDIS_LOGIN_LOCK_PREFIX;

    /** 登录失败次数最大值 */
    private static final int MAX_LOGIN_FAIL_COUNT = 5;

    /** 登录失败次数锁定时间（秒） */
    private static final int LOGIN_FAIL_LOCK_TIMEOUT = 1800;

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final InstitutionsMapper institutionsMapper;
    private final ConfigService configService;
    private final CaptchaService captchaService;
    private final StringRedisTemplate redisTemplate;

    /**
     * 用户登录（双 token 模式）
     * 支持使用用户名或邮箱登录
     *
     * @param account  用户名或邮箱
     * @param password 密码
     * @return 登录成功返回 accessToken + refreshToken，失败返回错误信息
     */
    public Map<String, Object> login(String account, String password) {
        if (account == null || account.isEmpty()) {
            throw new BusinessException("用户名或密码错误");
        }

        // 检查账号是否因失败次数过多被锁定
        String lockKey = LOGIN_LOCK_KEY + account;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(lockKey))) {
            long remainSeconds = redisTemplate.getExpire(lockKey);
            throw new BusinessException("登录失败次数过多，账号已锁定，请 " + remainSeconds + " 秒后重试");
        }

        UserEntity user;
        if (account.contains("@")) {
            user = userMapper.selectOne(
                    new QueryWrapper<UserEntity>().eq("email", account)
            );
        } else {
            user = userMapper.selectOne(
                    new QueryWrapper<UserEntity>().eq("username", account)
            );
        }
        if (user == null || !PASSWORD_ENCODER.matches(password, user.getPassword())) {
            int remainingAttempts = handleLoginFail(account);
            if (remainingAttempts > 0) {
                throw new BusinessException("用户名或密码错误，还可重试 " + remainingAttempts + " 次");
            }
            throw new BusinessException("登录失败次数过多，账号已锁定，请稍后再试");
        }

        // 登录成功，清除失败次数
        clearLoginFail(account);

        // 单设备登录：先踢掉该账号所有已登录的 token
        boolean singleDevice = configService.getBooleanConfigValue(SysConfigKeys.LOGIN_SINGLE_DEVICE, true);
        if (singleDevice) {
            StpUtil.logout(user.getId());
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
     * 处理登录失败：增加失败次数，达到阈值后锁定账号
     *
     * @return 剩余可尝试次数；返回 0 表示账号已被锁定
     */
    private int handleLoginFail(String account) {
        int maxAttempts = configService.getIntConfigValue(SysConfigKeys.LOGIN_MAX_ATTEMPTS, MAX_LOGIN_FAIL_COUNT);
        int lockDuration = configService.getIntConfigValue(SysConfigKeys.LOGIN_LOCK_DURATION, LOGIN_FAIL_LOCK_TIMEOUT);

        String failKey = LOGIN_FAIL_COUNT_KEY + account;
        Long failCount = redisTemplate.opsForValue().increment(failKey);
        if (failCount != null && failCount == 1L) {
            // 第一次失败时设置过期时间，避免 key 永久存在
            redisTemplate.expire(failKey, Duration.ofSeconds(lockDuration));
        }

        if (failCount != null && failCount >= maxAttempts) {
            redisTemplate.opsForValue().set(LOGIN_LOCK_KEY + account, "1", Duration.ofSeconds(lockDuration));
            redisTemplate.delete(failKey);
            return 0;
        }

        return maxAttempts - failCount.intValue();
    }

    /**
     * 登录成功后清除失败次数
     */
    private void clearLoginFail(String account) {
        redisTemplate.delete(LOGIN_FAIL_COUNT_KEY + account);
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
        ConfigEntity registerEnableConfig = configService.getConfigByKey(SysConfigKeys.REGISTER_ENABLE);
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
        ConfigEntity verifyCodeConfig = configService.getConfigByKey(SysConfigKeys.REGISTER_VERIFY_CODE_ENABLE);
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
        String defaultInstitutionCode = configService.getConfigValue(SysConfigKeys.REGISTER_DEFAULT_INSTITUTION, "main");
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
        String defaultRoleCode = configService.getConfigValue(SysConfigKeys.REGISTER_DEFAULT_ROLE, "common_user");
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
