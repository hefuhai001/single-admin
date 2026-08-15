package com.hfh.api.modules.monitor.redis.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.util.SaResult;
import com.hfh.api.base.BaseController;
import com.hfh.api.modules.monitor.redis.service.RedisMonitorService;
import com.hfh.api.modules.monitor.redis.vo.RedisCommandStatsVO;
import com.hfh.api.modules.monitor.redis.vo.RedisInfoVO;
import com.hfh.api.modules.monitor.redis.vo.RedisKeyDetailVO;
import com.hfh.api.modules.monitor.redis.vo.RedisKeyTreeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Redis 缓存监控控制器
 */
@RestController(value = "redisMonitorController")
@RequestMapping("/admin/monitor/redis")
@RequiredArgsConstructor
@Tag(name = "Redis缓存监控", description = "Redis 服务器信息、命令统计与 Key 管理")
public class RedisMonitorController extends BaseController {

    private final RedisMonitorService redisMonitorService;

    /**
     * 获取 Redis 服务器信息总览
     */
    @SaCheckPermission("monitor:redis:query")
    @GetMapping("/info")
    @Operation(summary = "获取 Redis 服务器信息总览")
    public SaResult info() {
        RedisInfoVO info = redisMonitorService.getRedisInfo();
        return SaResult.data(info);
    }

    /**
     * 获取命令统计
     */
    @SaCheckPermission("monitor:redis:query")
    @GetMapping("/command-stats")
    @Operation(summary = "获取 Redis 命令统计")
    public SaResult commandStats() {
        List<RedisCommandStatsVO> list = redisMonitorService.listCommandStats();
        return SaResult.data(list);
    }

    /**
     * 分页查询 Key 列表
     */
    @SaCheckPermission("monitor:redis:query")
    @GetMapping("/keys")
    @Operation(summary = "分页查询 Key 列表")
    public SaResult keys(
            @Parameter(description = "Key 匹配模式，支持通配符，默认 *；纯前缀时按前缀及其子级匹配")
            @RequestParam(defaultValue = "*") String pattern,
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size) {
        Map<String, Object> page = redisMonitorService.pageKeys(pattern, current, size);
        return SaResult.data(page);
    }

    /**
     * 获取 Key 树（Key 按冒号分层）
     */
    @SaCheckPermission("monitor:redis:query")
    @GetMapping("/keys/tree")
    @Operation(summary = "获取 Key 树")
    public SaResult keyTree() {
        List<RedisKeyTreeVO> tree = redisMonitorService.getKeyTree();
        return SaResult.data(tree);
    }

    /**
     * 获取 Key 详情
     */
    @SaCheckPermission("monitor:redis:query")
    @GetMapping("/keys/{key}")
    @Operation(summary = "获取 Key 详情")
    public SaResult keyDetail(@Parameter(description = "Key 名称") @PathVariable("key") String key) {
        RedisKeyDetailVO detail = redisMonitorService.getKeyDetail(key);
        return SaResult.data(detail);
    }

    /**
     * 批量删除 Key
     */
    @SaCheckPermission("monitor:redis:delete")
    @DeleteMapping("/keys")
    @Operation(summary = "批量删除 Key")
    public SaResult deleteKeys(@Parameter(description = "Key 名称数组") @RequestBody List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return SaResult.error("请选择要删除的 Key");
        }
        long count = redisMonitorService.deleteKeys(keys);
        return SaResult.ok("删除成功，共删除 " + count + " 个 Key");
    }
}
