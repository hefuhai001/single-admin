package com.hfh.api.modules.monitor.redis.controller;

import cn.dev33.satoken.util.SaResult;
import com.hfh.api.modules.monitor.redis.service.RedisMonitorService;
import com.hfh.api.modules.monitor.redis.vo.RedisCommandStatsVO;
import com.hfh.api.modules.monitor.redis.vo.RedisInfoVO;
import com.hfh.api.modules.monitor.redis.vo.RedisKeyDetailVO;
import com.hfh.api.modules.monitor.redis.vo.RedisKeyTreeVO;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RedisMonitorControllerTest {

    @Test
    void allEndpointsShouldDelegate() {
        RedisMonitorService service = mock(RedisMonitorService.class);
        RedisMonitorController controller = new RedisMonitorController(service);
        when(service.getRedisInfo()).thenReturn(new RedisInfoVO());
        when(service.listCommandStats()).thenReturn(List.of(new RedisCommandStatsVO()));
        when(service.pageKeys("k", 1, 10)).thenReturn(Map.of("total", 0));
        when(service.getKeyTree()).thenReturn(List.of(new RedisKeyTreeVO()));
        when(service.getKeyDetail("k")).thenReturn(new RedisKeyDetailVO());
        when(service.deleteKeys(List.of("k"))).thenReturn(2L);

        assertThat(controller.info().getData()).isNotNull();
        assertThat((List<?>) controller.commandStats().getData()).hasSize(1);
        assertThat(controller.keys("k", 1, 10).getData()).isNotNull();
        assertThat((List<?>) controller.keyTree().getData()).hasSize(1);
        assertThat(controller.keyDetail("k").getData()).isNotNull();
        assertThat(controller.deleteKeys(null).getMsg()).isEqualTo("请选择要删除的 Key");
        assertThat(controller.deleteKeys(List.of()).getMsg()).isEqualTo("请选择要删除的 Key");
        SaResult result = controller.deleteKeys(List.of("k"));
        assertThat(result.getMsg()).contains("2");

        verify(service).deleteKeys(List.of("k"));
    }
}
