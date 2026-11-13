package com.hfh.api.modules.monitor.redis.service.impl;

import com.hfh.api.exception.BusinessException;
import com.hfh.api.modules.monitor.redis.vo.RedisCommandStatsVO;
import com.hfh.api.modules.monitor.redis.vo.RedisInfoVO;
import com.hfh.api.modules.monitor.redis.vo.RedisKeyDetailVO;
import com.hfh.api.modules.monitor.redis.vo.RedisKeyTreeVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RedisMonitorServiceImplTest {

    @Mock
    private RedisConnectionFactory connectionFactory;
    @Mock
    private RedisConnection connection;

    private RedisMonitorServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new RedisMonitorServiceImpl(connectionFactory);
        when(connectionFactory.getConnection()).thenReturn(connection);
    }

    private Cursor<byte[]> cursor(String... keys) {
        @SuppressWarnings("unchecked")
        Cursor<byte[]> cursor = mock(Cursor.class);
        Boolean[] hasNext = new Boolean[keys.length + 1];
        for (int i = 0; i < keys.length; i++) {
            hasNext[i] = true;
        }
        hasNext[keys.length] = false;
        when(cursor.hasNext()).thenReturn(hasNext[0],
                java.util.Arrays.copyOfRange(hasNext, 1, hasNext.length));
        byte[][] values = new byte[keys.length][];
        for (int i = 0; i < keys.length; i++) {
            values[i] = keys[i].getBytes(StandardCharsets.UTF_8);
        }
        when(cursor.next()).thenReturn(values[0], java.util.Arrays.copyOfRange(values, 1, values.length));
        return cursor;
    }

    @Test
    void getRedisInfoShouldParseProperties() {
        Properties info = new Properties();
        info.setProperty("redis_version", "7.2.0");
        info.setProperty("redis_mode", "standalone");
        info.setProperty("uptime_in_seconds", "123");
        info.setProperty("used_memory", "1024");
        info.setProperty("maxmemory", "abc");
        info.setProperty("connected_clients", "5");
        info.setProperty("instantaneous_ops_per_sec", "9");
        info.setProperty("keyspace_hits", "11");
        info.setProperty("keyspace_misses", "2");
        when(connection.info()).thenReturn(info);
        when(connection.dbSize()).thenReturn(8L);

        RedisInfoVO vo = service.getRedisInfo();

        assertThat(vo.getVersion()).isEqualTo("7.2.0");
        assertThat(vo.getMode()).isEqualTo("standalone");
        assertThat(vo.getUptimeSeconds()).isEqualTo(123L);
        assertThat(vo.getUsedMemoryBytes()).isEqualTo(1024L);
        assertThat(vo.getMaxmemoryBytes()).isZero();
        assertThat(vo.getConnectedClients()).isEqualTo(5L);
        assertThat(vo.getInstantaneousOps()).isEqualTo(9L);
        assertThat(vo.getKeyspaceHits()).isEqualTo(11L);
        assertThat(vo.getKeyspaceMisses()).isEqualTo(2L);
        assertThat(vo.getTotalKeyCount()).isEqualTo(8L);
    }

    @Test
    void getRedisInfoShouldWrapException() {
        when(connectionFactory.getConnection()).thenThrow(new RuntimeException("down"));

        assertThatThrownBy(() -> service.getRedisInfo())
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Redis 连接失败");
    }

    @Test
    void listCommandStatsShouldParseAndSort() {
        Properties info = new Properties();
        info.setProperty("cmdstat_get", "calls=10,usec=100,usec_per_call=10.5");
        info.setProperty("cmdstat_set", "calls=20,usec=200,usec_per_call=10.0");
        info.setProperty("cmdstat_bad", "calls=3,broken");
        info.setProperty("not_a_command", "x");
        when(connection.info("commandstats")).thenReturn(info);

        List<RedisCommandStatsVO> list = service.listCommandStats();

        assertThat(list).hasSize(3);
        assertThat(list.get(0).getCommand()).isEqualTo("set");
        assertThat(list.get(0).getCalls()).isEqualTo(20L);
        assertThat(list.get(0).getUsec()).isEqualTo(200L);
        assertThat(list.get(0).getUsecPerCall()).isEqualTo(10.0);
        assertThat(list).anySatisfy(vo -> {
            if ("get".equals(vo.getCommand())) {
                assertThat(vo.getCalls()).isEqualTo(10L);
            }
        });
    }

    @Test
    void listCommandStatsShouldWrapException() {
        when(connection.info("commandstats")).thenThrow(new RuntimeException("boom"));

        assertThatThrownBy(() -> service.listCommandStats())
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void pageKeysShouldFilterByPrefixAndPaginate() {
        Cursor<byte[]> cursor = cursor("config", "config:a", "other", "config:a:b");
        when(connection.scan(any(ScanOptions.class))).thenReturn(cursor);
        when(connection.type(any())).thenReturn(DataType.STRING);
        when(connection.ttl(any())).thenReturn(100L);
        when(connection.strLen(any())).thenReturn(3L);

        Map<String, Object> result = service.pageKeys("config", 1, 10);

        assertThat(result).containsEntry("total", 3)
                .containsEntry("current", 1)
                .containsEntry("size", 10);
        assertThat((List<?>) result.get("records")).hasSize(3);
    }

    @Test
    void pageKeysShouldNotFilterWhenWildcardGiven() {
        Cursor<byte[]> cursor = cursor("config", "config:a", "other");
        when(connection.scan(any(ScanOptions.class))).thenReturn(cursor);
        when(connection.type(any())).thenReturn(DataType.LIST);
        when(connection.ttl(any())).thenReturn(5L);
        when(connection.lLen(any())).thenReturn(7L);

        Map<String, Object> result = service.pageKeys("config*", -1, 2);

        assertThat(result).containsEntry("total", 3).containsEntry("current", 1).containsEntry("size", 2);
    }

    @Test
    void pageKeysShouldReturnEmptyRecordsWhenPageOutOfRange() {
        Cursor<byte[]> cursor = cursor("a");
        when(connection.scan(any(ScanOptions.class))).thenReturn(cursor);
        when(connection.type(any())).thenReturn(DataType.STRING);
        when(connection.ttl(any())).thenReturn(-1L);
        when(connection.strLen(any())).thenReturn(1L);

        Map<String, Object> result = service.pageKeys("*", 5, 10);

        assertThat((List<?>) result.get("records")).isEmpty();
    }

    @Test
    void pageKeysShouldWrapScanFailure() {
        when(connection.scan(any(ScanOptions.class))).thenThrow(new RuntimeException("scan"));

        assertThatThrownBy(() -> service.pageKeys("*", 1, 10))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void getKeyTreeShouldBuildNamespaceTree() {
        Cursor<byte[]> cursor = cursor("a", "a:b", "a:b:c", "x:y");
        when(connection.scan(any(ScanOptions.class))).thenReturn(cursor);

        List<RedisKeyTreeVO> tree = service.getKeyTree();

        assertThat(tree).hasSize(2);
        RedisKeyTreeVO a = tree.stream().filter(n -> "a".equals(n.getLabel())).findFirst().orElseThrow();
        assertThat(a.getFullKey()).isEqualTo("a");
        assertThat(a.getLeaf()).isFalse();
        assertThat(a.getCount()).isEqualTo(3L);
        RedisKeyTreeVO b = a.getChildren().stream().filter(n -> "b".equals(n.getLabel())).findFirst().orElseThrow();
        assertThat(b.getPrefix()).isEqualTo("a:b");
        assertThat(b.getFullKey()).isEqualTo("a:b");
        RedisKeyTreeVO c = b.getChildren().get(0);
        assertThat(c.getLeaf()).isTrue();
        assertThat(c.getChildren()).isNull();
    }

    @Test
    void getKeyTreeShouldWrapFailure() {
        when(connection.scan(any(ScanOptions.class))).thenThrow(new RuntimeException("x"));

        assertThatThrownBy(() -> service.getKeyTree()).isInstanceOf(BusinessException.class);
    }

    @Test
    void getKeyDetailShouldPreviewStringValue() {
        String longValue = "v".repeat(260);
        when(connection.type(any())).thenReturn(DataType.STRING);
        when(connection.ttl(any())).thenReturn(60L);
        when(connection.strLen(any())).thenReturn((long) longValue.length());
        when(connection.get(any())).thenReturn(longValue.getBytes(StandardCharsets.UTF_8));

        RedisKeyDetailVO vo = service.getKeyDetail("k");

        assertThat(vo.getKey()).isEqualTo("k");
        assertThat(vo.getType()).isEqualTo("STRING");
        assertThat(vo.getTtl()).isEqualTo(60L);
        assertThat(vo.getSize()).isEqualTo(260L);
        assertThat(vo.getValuePreview()).hasSize(203).endsWith("...");
    }

    @Test
    void getKeyDetailShouldHandleHashAndNullValue() {
        when(connection.type(any())).thenReturn(DataType.HASH);
        when(connection.ttl(any())).thenReturn(-1L);
        when(connection.hLen(any())).thenReturn(4L);

        RedisKeyDetailVO vo = service.getKeyDetail("h");

        assertThat(vo.getType()).isEqualTo("HASH");
        assertThat(vo.getSize()).isEqualTo(4L);
        assertThat(vo.getValuePreview()).isNull();
    }

    @Test
    void getKeyDetailShouldHandleUnknownType() {
        when(connection.type(any())).thenReturn(null);

        RedisKeyDetailVO vo = service.getKeyDetail("none");

        assertThat(vo.getType()).isEqualTo("NONE");
        assertThat(vo.getSize()).isZero();
    }

    @Test
    void getKeyDetailShouldWrapFailure() {
        when(connection.type(any())).thenThrow(new RuntimeException("err"));

        assertThatThrownBy(() -> service.getKeyDetail("k")).isInstanceOf(BusinessException.class);
    }

    @Test
    void deleteKeysShouldHandleEmptyAndNormal() {
        assertThat(service.deleteKeys(null)).isZero();
        assertThat(service.deleteKeys(List.of())).isZero();

        when(connection.del(any(byte[][].class))).thenReturn(3L);
        assertThat(service.deleteKeys(List.of("a", "b", "c"))).isEqualTo(3L);

        when(connection.del(any(byte[][].class))).thenReturn(null);
        assertThat(service.deleteKeys(List.of("a"))).isZero();
    }

    @Test
    void deleteKeysShouldWrapFailure() {
        when(connection.del(any(byte[][].class))).thenThrow(new RuntimeException("e"));

        assertThatThrownBy(() -> service.deleteKeys(List.of("a")))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void getConnectionFailureShouldThrowBusinessException() {
        when(connectionFactory.getConnection()).thenThrow(new RuntimeException("no redis"));

        assertThatThrownBy(() -> service.pageKeys("*", 1, 1))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Redis 连接失败");
        assertThatThrownBy(() -> service.getKeyTree()).isInstanceOf(BusinessException.class);
    }
}
