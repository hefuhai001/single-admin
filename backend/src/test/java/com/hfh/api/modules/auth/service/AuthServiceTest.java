package com.hfh.api.modules.auth.service;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.session.TokenSign;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceTest {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    @Mock
    private UserMapper userMapper;
    @Mock
    private RoleMapper roleMapper;
    @Mock
    private UserRoleMapper userRoleMapper;
    @Mock
    private InstitutionsMapper institutionsMapper;
    @Mock
    private ConfigService configService;
    @Mock
    private CaptchaService captchaService;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    private AuthService authService;
    private MockedStatic<StpUtil> stp;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userMapper, roleMapper, userRoleMapper, institutionsMapper,
                configService, captchaService, redisTemplate);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        stp = mockStatic(StpUtil.class);
    }

    @AfterEach
    void tearDown() {
        stp.close();
    }

    private UserEntity user(String password) {
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setUsername("alice");
        user.setPassword(ENCODER.encode(password));
        return user;
    }

    private ConfigEntity config(String key, String value) {
        ConfigEntity entity = new ConfigEntity();
        entity.setConfigKey(key);
        entity.setConfigValue(value);
        entity.setStatus(Boolean.TRUE);
        return entity;
    }

    private void stubTokenPair() {
        SaTokenInfo tokenInfo = org.mockito.Mockito.mock(SaTokenInfo.class);
        when(tokenInfo.getTokenValue()).thenReturn("access-token");
        stp.when(StpUtil::getTokenInfo).thenReturn(tokenInfo);
        stp.when(StpUtil::getTokenValue).thenReturn("refresh-token");
        stp.when(() -> StpUtil.getTokenSessionByToken(anyString())).thenReturn(null);
    }

    @Test
    void loginShouldRejectEmptyAccount() {
        assertThatThrownBy(() -> authService.login("", "pwd", "dev", "127.0.0.1"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("用户名或密码错误");
        assertThatThrownBy(() -> authService.login(null, "pwd", "dev", "127.0.0.1"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void loginShouldRejectLockedAccount() {
        when(redisTemplate.hasKey("login:lock:alice")).thenReturn(true);
        when(redisTemplate.getExpire("login:lock:alice")).thenReturn(120L);

        assertThatThrownBy(() -> authService.login("alice", "pwd", "dev", "127.0.0.1"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("120");
    }

    @Test
    void loginShouldCountFailureWhenUserMissing() {
        when(userMapper.selectOne(any(Wrapper.class))).thenReturn(null);
        when(configService.getIntConfigValue(SysConfigKeys.LOGIN_MAX_ATTEMPTS, 5)).thenReturn(5);
        when(configService.getIntConfigValue(SysConfigKeys.LOGIN_LOCK_DURATION, 1800)).thenReturn(1800);
        when(valueOperations.increment("login:fail:alice")).thenReturn(1L);

        assertThatThrownBy(() -> authService.login("alice", "pwd", "dev", "127.0.0.1"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("用户名或密码错误，还可重试 4 次");
        verify(redisTemplate).expire("login:fail:alice", Duration.ofSeconds(1800));
    }

    @Test
    void loginShouldLockAccountWhenFailCountReached() {
        when(userMapper.selectOne(any(Wrapper.class))).thenReturn(user("right"));
        when(configService.getIntConfigValue(SysConfigKeys.LOGIN_MAX_ATTEMPTS, 5)).thenReturn(5);
        when(configService.getIntConfigValue(SysConfigKeys.LOGIN_LOCK_DURATION, 1800)).thenReturn(1800);
        when(valueOperations.increment("login:fail:alice")).thenReturn(5L);

        assertThatThrownBy(() -> authService.login("alice", "wrong", "dev", "127.0.0.1"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("登录失败次数过多，账号已锁定，请稍后再试");
        verify(valueOperations).set("login:lock:alice", "1", Duration.ofSeconds(1800));
        verify(redisTemplate).delete("login:fail:alice");
    }

    @Test
    void loginShouldSucceedByUsernameAndCreateTokenPair() {
        UserEntity user = user("right");
        when(userMapper.selectOne(any(Wrapper.class))).thenReturn(user);
        when(configService.getIntConfigValue(SysConfigKeys.LOGIN_MAX_ONLINE_DEVICES, 3)).thenReturn(0);
        SaSession accessSession = org.mockito.Mockito.mock(SaSession.class);
        stubTokenPair();
        stp.when(() -> StpUtil.getTokenSessionByToken("access-token")).thenReturn(accessSession);

        Map<String, Object> result = authService.login("alice", "right", "web-uuid", "10.0.0.1");

        assertThat(result).containsEntry("accessToken", "access-token")
                .containsEntry("refreshToken", "refresh-token");
        verify(redisTemplate).delete("login:fail:alice");
        verify(accessSession).set("loginIp", "10.0.0.1");
        stp.verify(() -> StpUtil.login(eq(1L), any(cn.dev33.satoken.stp.SaLoginModel.class)),
                org.mockito.Mockito.times(2));
    }

    @Test
    void loginShouldRejectWhenOnlineDevicesReached() {
        UserEntity user = user("right");
        user.setId(1L);
        when(userMapper.selectOne(any(Wrapper.class))).thenReturn(user);
        when(configService.getIntConfigValue(SysConfigKeys.LOGIN_MAX_ONLINE_DEVICES, 3)).thenReturn(1);

        SaSession session = org.mockito.Mockito.mock(SaSession.class);
        TokenSign sign = org.mockito.Mockito.mock(TokenSign.class);
        when(sign.getDevice()).thenReturn("other-device");
        when(session.getTokenSignList()).thenReturn(List.of(sign));
        stp.when(() -> StpUtil.getSessionByLoginId(1L, false)).thenReturn(session);

        assertThatThrownBy(() -> authService.login("alice", "right", "new-device", "ip"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("同时在线设备数量已达上限");
    }

    @Test
    void loginShouldAllowWhenCurrentDeviceAlreadyOnline() {
        UserEntity user = user("right");
        when(userMapper.selectOne(any(Wrapper.class))).thenReturn(user);
        when(configService.getIntConfigValue(SysConfigKeys.LOGIN_MAX_ONLINE_DEVICES, 3)).thenReturn(1);

        SaSession session = org.mockito.Mockito.mock(SaSession.class);
        TokenSign access = org.mockito.Mockito.mock(TokenSign.class);
        when(access.getDevice()).thenReturn("web");
        TokenSign refresh = org.mockito.Mockito.mock(TokenSign.class);
        when(refresh.getDevice()).thenReturn("refresh-web");
        when(session.getTokenSignList()).thenReturn(List.of(access, refresh));
        stp.when(() -> StpUtil.getSessionByLoginId(1L, false)).thenReturn(session);
        stubTokenPair();

        Map<String, Object> result = authService.login("alice", "right", "web", "ip");

        assertThat(result).containsKey("accessToken");
    }

    @Test
    void loginShouldSkipDeviceCheckWhenNoSession() {
        UserEntity user = user("right");
        when(userMapper.selectOne(any(Wrapper.class))).thenReturn(user);
        when(configService.getIntConfigValue(SysConfigKeys.LOGIN_MAX_ONLINE_DEVICES, 3)).thenReturn(3);
        stp.when(() -> StpUtil.getSessionByLoginId(1L, false)).thenReturn(null);
        stubTokenPair();

        assertThat(authService.login("alice", "right", null, null)).containsKey("refreshToken");
    }

    @Test
    void logoutShouldDoNothingWhenNotLoggedIn() {
        stp.when(StpUtil::isLogin).thenReturn(false);

        authService.logout();

        stp.verify(() -> StpUtil.logoutByTokenValue(anyString()), never());
    }

    @Test
    void logoutShouldRemoveRefreshAndAccessToken() {
        stp.when(StpUtil::isLogin).thenReturn(true);
        stp.when(StpUtil::getLoginId).thenReturn(1L);
        stp.when(StpUtil::getTokenValue).thenReturn("access");
        stp.when(StpUtil::getLoginDevice).thenReturn("web");
        SaSession session = org.mockito.Mockito.mock(SaSession.class);
        TokenSign refresh = org.mockito.Mockito.mock(TokenSign.class);
        when(refresh.getDevice()).thenReturn("refresh-web");
        when(refresh.getValue()).thenReturn("r1");
        when(session.getTokenSignList()).thenReturn(List.of(refresh));
        stp.when(() -> StpUtil.getSessionByLoginId(1L, false)).thenReturn(session);

        authService.logout();

        stp.verify(() -> StpUtil.logoutByTokenValue("r1"));
        stp.verify(() -> StpUtil.logoutByTokenValue("access"));
    }

    @Test
    void logoutShouldHandleMissingSession() {
        stp.when(StpUtil::isLogin).thenReturn(true);
        stp.when(StpUtil::getLoginId).thenReturn(1L);
        stp.when(StpUtil::getTokenValue).thenReturn("access");
        stp.when(StpUtil::getLoginDevice).thenReturn("web");
        stp.when(() -> StpUtil.getSessionByLoginId(1L, false)).thenReturn(null);

        authService.logout();

        stp.verify(() -> StpUtil.logoutByTokenValue("access"));
    }

    @Test
    void refreshShouldRejectExpiredToken() {
        stp.when(() -> StpUtil.getLoginIdByToken("bad")).thenReturn(null);

        assertThatThrownBy(() -> authService.refresh("bad", "ip"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("已失效");
    }

    @Test
    void refreshShouldRejectNonRefreshToken() {
        stp.when(() -> StpUtil.getLoginIdByToken("token")).thenReturn(1L);
        SaSession session = org.mockito.Mockito.mock(SaSession.class);
        when(session.get("isRefresh")).thenReturn(null);
        stp.when(() -> StpUtil.getTokenSessionByToken("token")).thenReturn(session);

        assertThatThrownBy(() -> authService.refresh("token", "ip"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("无效的 refreshToken");
    }

    @Test
    void refreshShouldIssueNewTokenPair() {
        stp.when(() -> StpUtil.getLoginIdByToken("old-refresh")).thenReturn(1L);
        stubTokenPair();
        SaSession oldSession = org.mockito.Mockito.mock(SaSession.class);
        when(oldSession.get("isRefresh")).thenReturn(true);
        when(oldSession.getString("deviceId")).thenReturn("web-uuid");
        stp.when(() -> StpUtil.getTokenSessionByToken("old-refresh")).thenReturn(oldSession);

        Map<String, Object> result = authService.refresh("old-refresh", "ip");

        assertThat(result).containsEntry("accessToken", "access-token");
        stp.verify(() -> StpUtil.logoutByTokenValue("old-refresh"));
    }

    @Test
    void registerShouldRejectWhenDisabled() {
        when(configService.getConfigByKey(SysConfigKeys.REGISTER_ENABLE)).thenReturn(null);

        assertThatThrownBy(() -> authService.register(new RegisterDTO()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("注册功能已关闭");

        when(configService.getConfigByKey(SysConfigKeys.REGISTER_ENABLE))
                .thenReturn(config(SysConfigKeys.REGISTER_ENABLE, "false"));
        assertThatThrownBy(() -> authService.register(new RegisterDTO()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("注册功能已关闭");
    }

    @Test
    void registerShouldRejectPasswordMismatch() {
        when(configService.getConfigByKey(SysConfigKeys.REGISTER_ENABLE))
                .thenReturn(config(SysConfigKeys.REGISTER_ENABLE, "true"));
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("bob");
        dto.setPassword("a");
        dto.setConfirmPassword("b");

        assertThatThrownBy(() -> authService.register(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessage("两次输入的密码不一致");
    }

    @Test
    void registerShouldRejectExistingUsername() {
        when(configService.getConfigByKey(SysConfigKeys.REGISTER_ENABLE))
                .thenReturn(config(SysConfigKeys.REGISTER_ENABLE, "true"));
        when(userMapper.selectOne(any(Wrapper.class))).thenReturn(user("pwd"));
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("alice");
        dto.setPassword("p");
        dto.setConfirmPassword("p");

        assertThatThrownBy(() -> authService.register(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessage("用户名已存在");
    }

    @Test
    void registerShouldRejectExistingEmail() {
        when(configService.getConfigByKey(SysConfigKeys.REGISTER_ENABLE))
                .thenReturn(config(SysConfigKeys.REGISTER_ENABLE, "true"));
        when(userMapper.selectOne(any(Wrapper.class))).thenReturn(null, user("pwd"));
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("bob");
        dto.setPassword("p");
        dto.setConfirmPassword("p");
        dto.setEmail("bob@example.com");

        assertThatThrownBy(() -> authService.register(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessage("邮箱已被注册");
    }

    @Test
    void registerShouldRequireVerifyCodeWhenEnabled() {
        when(configService.getConfigByKey(SysConfigKeys.REGISTER_ENABLE))
                .thenReturn(config(SysConfigKeys.REGISTER_ENABLE, "true"));
        when(configService.getConfigByKey(SysConfigKeys.REGISTER_VERIFY_CODE_ENABLE))
                .thenReturn(config(SysConfigKeys.REGISTER_VERIFY_CODE_ENABLE, "true"));
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("bob");
        dto.setPassword("p");
        dto.setConfirmPassword("p");

        assertThatThrownBy(() -> authService.register(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessage("邮箱验证码不能为空");

        dto.setVerifyCode("123456");
        when(captchaService.verifyEmailCode(null, "123456")).thenReturn(false);
        assertThatThrownBy(() -> authService.register(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessage("邮箱验证码错误或已过期");
    }

    @Test
    void registerShouldSaveUserWithDefaultInstitutionAndRole() {
        when(configService.getConfigByKey(SysConfigKeys.REGISTER_ENABLE))
                .thenReturn(config(SysConfigKeys.REGISTER_ENABLE, "true"));
        when(configService.getConfigByKey(SysConfigKeys.REGISTER_VERIFY_CODE_ENABLE))
                .thenReturn(config(SysConfigKeys.REGISTER_VERIFY_CODE_ENABLE, "true"));
        when(captchaService.verifyEmailCode("bob@example.com", "123456")).thenReturn(true);
        when(userMapper.selectOne(any(Wrapper.class))).thenReturn(null);
        when(configService.getConfigValue(SysConfigKeys.REGISTER_DEFAULT_INSTITUTION, "main")).thenReturn("main");
        when(configService.getConfigValue(SysConfigKeys.REGISTER_DEFAULT_ROLE, "common_user")).thenReturn("common_user");
        InstitutionsEntity institution = new InstitutionsEntity();
        institution.setId(5L);
        when(institutionsMapper.selectOne(any(Wrapper.class))).thenReturn(institution);
        RoleEntity role = new RoleEntity();
        role.setId(7L);
        when(roleMapper.selectOne(any(Wrapper.class))).thenReturn(role);
        when(userMapper.insert(any(UserEntity.class))).thenAnswer(inv -> {
            inv.<UserEntity>getArgument(0).setId(99L);
            return 1;
        });
        stubTokenPair();

        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("bob");
        dto.setPassword("secret");
        dto.setConfirmPassword("secret");
        dto.setEmail("bob@example.com");
        dto.setVerifyCode("123456");
        dto.setDeviceId("dev");

        Map<String, Object> result = authService.register(dto);

        assertThat(result).containsKeys("accessToken", "refreshToken");
        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userMapper).insert(userCaptor.capture());
        UserEntity saved = userCaptor.getValue();
        assertThat(saved.getInstitutionId()).isEqualTo(5L);
        assertThat(saved.getStatus()).isEqualTo(1);
        assertThat(ENCODER.matches("secret", saved.getPassword())).isTrue();
        ArgumentCaptor<UserRoleEntity> roleCaptor = ArgumentCaptor.forClass(UserRoleEntity.class);
        verify(userRoleMapper).insert(roleCaptor.capture());
        assertThat(roleCaptor.getValue().getUserId()).isEqualTo(99L);
        assertThat(roleCaptor.getValue().getRoleId()).isEqualTo(7L);
    }

    @Test
    void registerShouldSkipDefaultInstitutionAndRoleWhenMissing() {
        when(configService.getConfigByKey(SysConfigKeys.REGISTER_ENABLE))
                .thenReturn(config(SysConfigKeys.REGISTER_ENABLE, "true"));
        when(userMapper.selectOne(any(Wrapper.class))).thenReturn(null);
        when(configService.getConfigValue(SysConfigKeys.REGISTER_DEFAULT_INSTITUTION, "main")).thenReturn("");
        when(configService.getConfigValue(SysConfigKeys.REGISTER_DEFAULT_ROLE, "common_user")).thenReturn("common_user");
        when(roleMapper.selectOne(any(Wrapper.class))).thenReturn(null);
        stubTokenPair();

        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("bob");
        dto.setPassword("p");
        dto.setConfirmPassword("p");

        authService.register(dto);

        verify(institutionsMapper, never()).selectOne(any(Wrapper.class));
        verify(userRoleMapper, never()).insert(any(UserRoleEntity.class));
    }
}
