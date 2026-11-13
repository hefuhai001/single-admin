package com.hfh.api.modules.monitor.online.service.impl;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.session.TokenSign;
import cn.dev33.satoken.stp.StpUtil;
import com.hfh.api.modules.monitor.online.vo.OnlineUserVO;
import com.hfh.api.modules.system.entity.UserEntity;
import com.hfh.api.modules.system.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OnlineUserServiceImplTest {

    @Mock
    private UserService userService;

    private OnlineUserServiceImpl service;
    private MockedStatic<StpUtil> stp;

    @BeforeEach
    void setUp() {
        service = new OnlineUserServiceImpl(userService);
        stp = mockStatic(StpUtil.class);
    }

    @AfterEach
    void tearDown() {
        stp.close();
    }

    private TokenSign sign(String device, String value) {
        TokenSign sign = mock(TokenSign.class);
        when(sign.getDevice()).thenReturn(device);
        when(sign.getValue()).thenReturn(value);
        return sign;
    }

    @Test
    void listOnlineUsersShouldReturnEmptyWhenNoSessions() {
        stp.when(() -> StpUtil.searchSessionId(anyString(), anyInt(), anyInt(), anyBoolean()))
                .thenReturn(List.of());

        assertThat(service.listOnlineUsers()).isEmpty();
    }

    @Test
    void listOnlineUsersShouldSkipMissingSessionsAndLoginIds() {
        stp.when(() -> StpUtil.searchSessionId(anyString(), anyInt(), anyInt(), anyBoolean()))
                .thenReturn(List.of("s1", "s2", "s3"));
        stp.when(() -> StpUtil.getSessionBySessionId("s1")).thenReturn(null);
        SaSession noLoginId = mock(SaSession.class);
        when(noLoginId.getLoginId()).thenReturn(null);
        stp.when(() -> StpUtil.getSessionBySessionId("s2")).thenReturn(noLoginId);
        SaSession session = mock(SaSession.class);
        when(session.getLoginId()).thenReturn(1L);
        when(session.getCreateTime()).thenReturn(System.currentTimeMillis());
        TokenSign refreshSign = sign("refresh-web", "r1");
        TokenSign accessSign = sign("web", "a1");
        when(session.getTokenSignList()).thenReturn(List.of(refreshSign, accessSign));
        stp.when(() -> StpUtil.getSessionBySessionId("s3")).thenReturn(session);

        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setUsername("alice");
        user.setNickname("Alice");
        when(userService.getById(1L)).thenReturn(user);
        SaSession tokenSession = mock(SaSession.class);
        when(tokenSession.get("loginIp")).thenReturn("10.0.0.1");
        stp.when(() -> StpUtil.getTokenSessionByToken("a1")).thenReturn(tokenSession);

        List<OnlineUserVO> result = service.listOnlineUsers();

        assertThat(result).hasSize(1);
        OnlineUserVO vo = result.get(0);
        assertThat(vo.getUsername()).isEqualTo("alice");
        assertThat(vo.getNickname()).isEqualTo("Alice");
        assertThat(vo.getDeviceId()).isEqualTo("web");
        assertThat(vo.getLoginIp()).isEqualTo("10.0.0.1");
        assertThat(vo.getToken()).isEqualTo("a1");
        assertThat(vo.getUserId()).isEqualTo(1L);
    }

    @Test
    void listOnlineUsersShouldHandleUnknownUserAndMissingIp() {
        stp.when(() -> StpUtil.searchSessionId(anyString(), anyInt(), anyInt(), anyBoolean()))
                .thenReturn(List.of("s1"));
        SaSession session = mock(SaSession.class);
        when(session.getLoginId()).thenReturn("bad-id");
        when(session.getCreateTime()).thenReturn(0L);
        TokenSign accessSign = sign(null, "a1");
        when(session.getTokenSignList()).thenReturn(List.of(accessSign));
        stp.when(() -> StpUtil.getSessionBySessionId("s1")).thenReturn(session);
        stp.when(() -> StpUtil.getTokenSessionByToken("a1")).thenReturn(null);

        List<OnlineUserVO> result = service.listOnlineUsers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isNull();
        assertThat(result.get(0).getUsername()).isEqualTo("-");
        assertThat(result.get(0).getNickname()).isEqualTo("-");
        assertThat(result.get(0).getLoginIp()).isEqualTo("-");
    }

    @Test
    void kickShouldRejectBlankToken() {
        assertThatThrownBy(() -> service.kickByToken(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.kickByToken("")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void kickShouldLogoutRefreshTokensFromBothSessions() {
        SaSession tokenSession = mock(SaSession.class);
        when(tokenSession.get("refreshToken")).thenReturn("r0");
        stp.when(() -> StpUtil.getTokenSessionByToken("a1")).thenReturn(tokenSession);
        stp.when(() -> StpUtil.getLoginIdByToken("a1")).thenReturn(1L);

        SaSession accountSession = mock(SaSession.class);
        TokenSign access = sign("web", "a1");
        TokenSign refresh = sign("refresh-web", "r1");
        when(accountSession.getTokenSignList()).thenReturn(List.of(access, refresh));
        stp.when(() -> StpUtil.getSessionByLoginId(eq(1L), anyBoolean())).thenReturn(accountSession);

        service.kickByToken("a1");

        stp.verify(() -> StpUtil.logoutByTokenValue("r0"));
        stp.verify(() -> StpUtil.logoutByTokenValue("r1"));
        stp.verify(() -> StpUtil.logoutByTokenValue("a1"));
    }

    @Test
    void kickShouldOnlyLogoutTokenWhenNoSessions() {
        stp.when(() -> StpUtil.getTokenSessionByToken("a1")).thenReturn(null);
        stp.when(() -> StpUtil.getLoginIdByToken("a1")).thenReturn(null);

        service.kickByToken("a1");

        stp.verify(() -> StpUtil.logoutByTokenValue("a1"));
    }

    @Test
    void kickShouldHandleSessionWithoutMatchingDevice() {
        SaSession tokenSession = mock(SaSession.class);
        when(tokenSession.get("refreshToken")).thenReturn("");
        stp.when(() -> StpUtil.getTokenSessionByToken("a1")).thenReturn(tokenSession);
        stp.when(() -> StpUtil.getLoginIdByToken("a1")).thenReturn(2L);
        SaSession accountSession = mock(SaSession.class);
        TokenSign other = sign("other", "a2");
        when(accountSession.getTokenSignList()).thenReturn(List.of(other));
        stp.when(() -> StpUtil.getSessionByLoginId(eq(2L), anyBoolean())).thenReturn(accountSession);

        service.kickByToken("a1");

        stp.verify(() -> StpUtil.logoutByTokenValue("a1"));
        stp.verify(() -> StpUtil.logoutByTokenValue("a2"), never());
    }
}
