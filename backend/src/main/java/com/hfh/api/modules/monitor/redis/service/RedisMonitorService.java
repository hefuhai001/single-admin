package com.hfh.api.modules.monitor.redis.service;

import com.hfh.api.modules.monitor.redis.vo.RedisCommandStatsVO;
import com.hfh.api.modules.monitor.redis.vo.RedisInfoVO;
import com.hfh.api.modules.monitor.redis.vo.RedisKeyDetailVO;
import com.hfh.api.modules.monitor.redis.vo.RedisKeyTreeVO;

import java.util.List;
import java.util.Map;

/**
 * Redis 缓存监控服务
 */
public interface RedisMonitorService {

    /**
     * 获取 Redis 服务器信息总览
     */
    RedisInfoVO getRedisInfo();

    /**
     * 获取命令统计（按调用次数降序）
     */
    List<RedisCommandStatsVO> listCommandStats();

    /**
     * 获取 Key 树（Key 按冒号分层构建命名空间树）
     */
    List<RedisKeyTreeVO> getKeyTree();

    /**
     * 分页查询 Key 列表（SCAN 扫描 + 内存分页）
     * <p>pattern 含通配符时按通配符匹配；否则视为前缀，匹配该前缀本身及其子级 Key</p>
     *
     * @param pattern Key 匹配模式，默认 *，支持 Redis 通配符
     * @param current 当前页，默认 1
     * @param size    每页大小，默认 10
     */
    Map<String, Object> pageKeys(String pattern, Integer current, Integer size);

    /**
     * 获取 Key 详情
     */
    RedisKeyDetailVO getKeyDetail(String key);

    /**
     * 批量删除 Key
     *
     * @return 实际删除数量
     */
    long deleteKeys(List<String> keys);
}
