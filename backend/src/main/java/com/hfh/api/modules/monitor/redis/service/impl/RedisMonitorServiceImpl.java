package com.hfh.api.modules.monitor.redis.service.impl;

import com.hfh.api.exception.BusinessException;
import com.hfh.api.modules.monitor.redis.service.RedisMonitorService;
import com.hfh.api.modules.monitor.redis.vo.RedisCommandStatsVO;
import com.hfh.api.modules.monitor.redis.vo.RedisInfoVO;
import com.hfh.api.modules.monitor.redis.vo.RedisKeyDetailVO;
import com.hfh.api.modules.monitor.redis.vo.RedisKeyTreeVO;
import com.hfh.api.modules.monitor.redis.vo.RedisKeyVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Redis 缓存监控服务实现
 * <p>通过 {@link RedisConnectionFactory} 直接执行原生命令（INFO/DBSIZE/SCAN 等）获取监控数据</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisMonitorServiceImpl implements RedisMonitorService {

    private static final String DEFAULT_PATTERN = "*";

    private final RedisConnectionFactory connectionFactory;

    /**
     * 获取 Redis 服务器信息总览
     */
    @Override
    public RedisInfoVO getRedisInfo() {
        try (RedisConnection connection = getConnection()) {
            Properties info = connection.info();
            RedisInfoVO vo = new RedisInfoVO();
            vo.setVersion(info.getProperty("redis_version"));
            vo.setMode(info.getProperty("redis_mode"));
            vo.setUptimeSeconds(parseLong(info.getProperty("uptime_in_seconds"), 0L));
            vo.setUsedMemoryBytes(parseLong(info.getProperty("used_memory"), 0L));
            vo.setMaxmemoryBytes(parseLong(info.getProperty("maxmemory"), 0L));
            vo.setConnectedClients(parseLong(info.getProperty("connected_clients"), 0L));
            vo.setInstantaneousOps(parseLong(info.getProperty("instantaneous_ops_per_sec"), 0L));
            vo.setKeyspaceHits(parseLong(info.getProperty("keyspace_hits"), 0L));
            vo.setKeyspaceMisses(parseLong(info.getProperty("keyspace_misses"), 0L));
            vo.setTotalKeyCount(connection.dbSize());
            return vo;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取 Redis 信息失败", e);
            throw new BusinessException("Redis 连接失败: " + e.getMessage());
        }
    }

    /**
     * 获取命令统计（按调用次数降序）
     */
    @Override
    public List<RedisCommandStatsVO> listCommandStats() {
        try (RedisConnection connection = getConnection()) {
            Properties info = connection.info("commandstats");
            List<RedisCommandStatsVO> list = new ArrayList<>();
            for (Map.Entry<Object, Object> entry : info.entrySet()) {
                String key = String.valueOf(entry.getKey());
                if (!key.startsWith("cmdstat_")) {
                    continue;
                }
                String command = key.substring("cmdstat_".length());
                String value = String.valueOf(entry.getValue());
                // value 形如：calls=123,usec=456,usec_per_call=3.70
                RedisCommandStatsVO vo = new RedisCommandStatsVO();
                vo.setCommand(command);
                for (String part : value.split(",")) {
                    String[] kv = part.split("=");
                    if (kv.length != 2) {
                        continue;
                    }
                    switch (kv[0].trim()) {
                        case "calls" -> vo.setCalls(parseLong(kv[1].trim(), 0L));
                        case "usec" -> vo.setUsec(parseLong(kv[1].trim(), 0L));
                        case "usec_per_call" -> vo.setUsecPerCall(parseDouble(kv[1].trim(), 0d));
                        default -> {
                        }
                    }
                }
                list.add(vo);
            }
            list.sort((a, b) -> Long.compare(b.getCalls(), a.getCalls()));
            return list;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取 Redis 命令统计失败", e);
            throw new BusinessException("Redis 连接失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询 Key 列表（SCAN 扫描 + 内存分页）
     */
    @Override
    public Map<String, Object> pageKeys(String pattern, Integer current, Integer size) {
        int page = current == null || current < 1 ? 1 : current;
        int pageSize = size == null || size < 1 ? 10 : size;
        String raw = pattern == null || pattern.isEmpty() ? DEFAULT_PATTERN : pattern;
        // 纯前缀（不含通配符）时按前缀精确过滤，避免误匹配同级其他命名空间
        boolean isPrefix = !hasWildcard(raw);
        String match = isPrefix ? raw + "*" : raw;
        String prefixFilter = isPrefix ? raw : null;

        List<RedisKeyVO> all;
        try (RedisConnection connection = getConnection()) {
            all = scanKeys(connection, match, prefixFilter);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("扫描 Redis Key 失败", e);
            throw new BusinessException("Redis 连接失败: " + e.getMessage());
        }

        int total = all.size();
        int from = (page - 1) * pageSize;
        int to = Math.min(from + pageSize, total);
        List<RedisKeyVO> records = from >= total ? Collections.emptyList() : all.subList(from, to);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("records", records);
        result.put("total", total);
        result.put("current", page);
        result.put("size", pageSize);
        return result;
    }

    /**
     * 获取 Key 树（Key 按冒号分层构建命名空间树）
     */
    @Override
    public List<RedisKeyTreeVO> getKeyTree() {
        try (RedisConnection connection = getConnection()) {
            return buildKeyTree(scanKeyNames(connection, "*"));
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取 Redis Key 树失败", e);
            throw new BusinessException("Redis 连接失败: " + e.getMessage());
        }
    }

    /**
     * 判断 pattern 是否包含通配符
     */
    private boolean hasWildcard(String pattern) {
        return pattern.indexOf('*') >= 0 || pattern.indexOf('?') >= 0 || pattern.indexOf('[') >= 0;
    }

    /**
     * 仅扫描 Key 名称（不查询类型/TTL/长度，用于构建树）
     */
    private List<String> scanKeyNames(RedisConnection connection, String match) {
        List<String> keys = new ArrayList<>();
        ScanOptions options = ScanOptions.scanOptions().match(match).count(1000).build();
        try (Cursor<byte[]> cursor = connection.scan(options)) {
            while (cursor.hasNext()) {
                keys.add(new String(cursor.next(), StandardCharsets.UTF_8));
            }
        }
        return keys;
    }

    /**
     * 按冒号分段构建 Key 命名空间树
     */
    private List<RedisKeyTreeVO> buildKeyTree(List<String> keys) {
        RedisKeyTreeVO root = new RedisKeyTreeVO();
        root.setLabel("全部");
        root.setPrefix("");
        root.setCount(0L);
        root.setLeaf(false);
        root.setChildren(new ArrayList<>());

        Set<String> keySet = new HashSet<>(keys);
        for (String key : keys) {
            insertNode(root, key.split(":"), 0, keySet);
        }

        cleanupEmptyChildren(root);
        return root.getChildren();
    }

    /**
     * 将 Key 的分段依次插入树中，沿途节点计数 +1，节点本身是真实 Key 时标记 fullKey
     */
    private void insertNode(RedisKeyTreeVO node, String[] parts, int index, Set<String> keySet) {
        // 当前前缀本身是一个真实 Key（如 config 既是命名空间又是独立 Key）
        if (keySet.contains(node.getPrefix())) {
            node.setFullKey(node.getPrefix());
        }
        node.setCount(node.getCount() + 1);
        if (index >= parts.length) {
            return;
        }
        RedisKeyTreeVO child = findChild(node, parts[index]);
        if (child == null) {
            child = new RedisKeyTreeVO();
            child.setLabel(parts[index]);
            child.setPrefix(node.getPrefix().isEmpty() ? parts[index] : node.getPrefix() + ":" + parts[index]);
            child.setCount(0L);
            child.setChildren(new ArrayList<>());
            node.getChildren().add(child);
        }
        insertNode(child, parts, index + 1, keySet);
    }

    /**
     * 查找指定名称的子节点
     */
    private RedisKeyTreeVO findChild(RedisKeyTreeVO node, String label) {
        if (node.getChildren() == null) {
            return null;
        }
        for (RedisKeyTreeVO child : node.getChildren()) {
            if (child.getLabel().equals(label)) {
                return child;
            }
        }
        return null;
    }

    /**
     * 清理空 children（置 null 并标记 leaf，避免前端渲染多余展开箭头）
     */
    private void cleanupEmptyChildren(RedisKeyTreeVO node) {
        if (node.getChildren() == null) {
            node.setLeaf(true);
            return;
        }
        if (node.getChildren().isEmpty()) {
            node.setChildren(null);
            node.setLeaf(true);
            return;
        }
        for (RedisKeyTreeVO child : node.getChildren()) {
            cleanupEmptyChildren(child);
        }
        node.setLeaf(false);
    }

    /**
     * 获取 Key 详情
     */
    @Override
    public RedisKeyDetailVO getKeyDetail(String key) {
        try (RedisConnection connection = getConnection()) {
            byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
            DataType type = connection.type(keyBytes);
            RedisKeyDetailVO vo = new RedisKeyDetailVO();
            vo.setKey(key);
            vo.setType(type != null ? type.name() : "NONE");
            vo.setTtl(connection.ttl(keyBytes));
            vo.setSize(getSize(connection, keyBytes, type));
            // String 类型展示值预览，取前 200 字符
            if (type == DataType.STRING) {
                byte[] value = connection.get(keyBytes);
                if (value != null) {
                    String preview = new String(value, StandardCharsets.UTF_8);
                    if (preview.length() > 200) {
                        preview = preview.substring(0, 200) + "...";
                    }
                    vo.setValuePreview(preview);
                }
            }
            return vo;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取 Redis Key 详情失败, key={}", key, e);
            throw new BusinessException("Redis 连接失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除 Key
     */
    @Override
    public long deleteKeys(List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return 0L;
        }
        try (RedisConnection connection = getConnection()) {
            byte[][] keyBytes = keys.stream()
                    .map(k -> k.getBytes(StandardCharsets.UTF_8))
                    .toArray(byte[][]::new);
            Long count = connection.del(keyBytes);
            return count == null ? 0L : count;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除 Redis Key 失败", e);
            throw new BusinessException("Redis 连接失败: " + e.getMessage());
        }
    }

    /**
     * SCAN 游标扫描匹配 Key，并填充类型/TTL/长度
     *
     * @param prefixFilter 非空时仅保留该前缀本身及其子级 Key（如 config、config:key）
     */
    private List<RedisKeyVO> scanKeys(RedisConnection connection, String match, String prefixFilter) {
        List<RedisKeyVO> list = new ArrayList<>();
        ScanOptions options = ScanOptions.scanOptions().match(match).count(1000).build();
        try (Cursor<byte[]> cursor = connection.scan(options)) {
            while (cursor.hasNext()) {
                byte[] keyBytes = cursor.next();
                String key = new String(keyBytes, StandardCharsets.UTF_8);
                // 纯前缀过滤：匹配前缀本身或其子级 Key，避免误匹配同级命名空间
                if (prefixFilter != null && !key.equals(prefixFilter) && !key.startsWith(prefixFilter + ":")) {
                    continue;
                }
                RedisKeyVO vo = new RedisKeyVO();
                vo.setKey(key);
                DataType type = connection.type(keyBytes);
                vo.setType(type != null ? type.name() : "NONE");
                vo.setTtl(connection.ttl(keyBytes));
                vo.setSize(getSize(connection, keyBytes, type));
                list.add(vo);
            }
        }
        return list;
    }

    /**
     * 按类型获取值长度
     */
    private long getSize(RedisConnection connection, byte[] keyBytes, DataType type) {
        if (type == null) {
            return 0L;
        }
        return switch (type) {
            case STRING -> connection.strLen(keyBytes);
            case LIST -> connection.lLen(keyBytes);
            case SET -> connection.sCard(keyBytes);
            case HASH -> connection.hLen(keyBytes);
            case ZSET -> connection.zCard(keyBytes);
            default -> 0L;
        };
    }

    /**
     * 获取连接，失败时抛出业务异常
     */
    private RedisConnection getConnection() {
        try {
            return connectionFactory.getConnection();
        } catch (Exception e) {
            log.error("获取 Redis 连接失败", e);
            throw new BusinessException("Redis 连接失败: " + e.getMessage());
        }
    }

    private long parseLong(String value, long defaultValue) {
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private double parseDouble(String value, double defaultValue) {
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
