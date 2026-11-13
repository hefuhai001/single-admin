package com.hfh.api.modules.monitor.online.controller;

import com.hfh.api.modules.monitor.online.service.OnlineUserService;
import com.hfh.api.modules.monitor.online.vo.OnlineUserVO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OnlineUserControllerTest {

    @Test
    void listAndKickShouldDelegate() {
        OnlineUserService service = mock(OnlineUserService.class);
        OnlineUserController controller = new OnlineUserController(service);
        when(service.listOnlineUsers()).thenReturn(List.of(new OnlineUserVO()));

        assertThat((List<?>) controller.list().getData()).hasSize(1);
        assertThat(controller.kick(null).getMsg()).isEqualTo("token 不能为空");
        assertThat(controller.kick("").getMsg()).isEqualTo("token 不能为空");
        assertThat(controller.kick("t").getMsg()).isEqualTo("踢人下线成功");

        verify(service).kickByToken("t");
    }
}
