package com.hfh.api.modules.auth.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.hfh.api.modules.auth.dto.RegisterDTO;
import com.hfh.api.modules.auth.service.AuthService;
import com.hfh.api.modules.system.entity.UserEntity;
import com.hfh.api.modules.system.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthControllerTest {

    private AuthService authService;
    private UserService userService;
    private AuthController controller;
    private MockedStatic<StpUtil> stp;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        userService = mock(UserService.class);
        controller = new AuthController(authService);
        ReflectionTestUtils.setField(controller, "userService", userService);
        stp = mockStatic(StpUtil.class);
    }

    @AfterEach
    void tearDown() {
        stp.close();
    }

    @Test
    void profileShouldReturnCurrentUserOrNull() {
        stp.when(StpUtil::isLogin).thenReturn(false);
        assertThat(controller.profile().getData()).isNull();

        stp.when(StpUtil::isLogin).thenReturn(true);
        stp.when(StpUtil::getLoginIdAsLong).thenReturn(1L);
        UserEntity user = new UserEntity();
        user.setUsername("alice");
        when(userService.getById(1L)).thenReturn(user);

        assertThat(controller.profile().getData()).isSameAs(user);
    }

    @Test
    void loginShouldPassRemoteIp() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn("10.0.0.1");
        when(authService.login("alice", "pwd", "dev", "10.0.0.1")).thenReturn(Map.of("accessToken", "a"));

        SaResult result = controller.login("alice", "pwd", "dev", request);

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertThat(data).containsEntry("accessToken", "a");
        verify(authService).login("alice", "pwd", "dev", "10.0.0.1");
    }

    @Test
    void refreshShouldDelegate() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(authService.refresh("r", "127.0.0.1")).thenReturn(Map.of("accessToken", "a"));

        assertThat(controller.refresh("r", request).getData()).isNotNull();
        verify(authService).refresh(eq("r"), eq("127.0.0.1"));
    }

    @Test
    void logoutRegisterAndIsLoginShouldDelegate() {
        stp.when(StpUtil::isLogin).thenReturn(true);
        when(authService.register(any(RegisterDTO.class))).thenReturn(Map.of("accessToken", "a"));

        assertThat(controller.logout().getCode()).isEqualTo(200);
        assertThat(controller.isLogin().getData()).isEqualTo(true);
        assertThat(controller.register(new RegisterDTO()).getData()).isNotNull();
        verify(authService).logout();
    }
}
