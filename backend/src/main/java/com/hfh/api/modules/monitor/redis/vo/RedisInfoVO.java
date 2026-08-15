package com.hfh.api.modules.monitor.redis.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Redis 服务器信息总览
 */
@Data
@Schema(description = "Redis 服务器信息总览")
public class RedisInfoVO {

    /** Redis 版本 */
    @Schema(description = "Redis 版本")
    private String version;

    /** 运行模式（standalone/cluster/sentinel） */
    @Schema(description = "运行模式")
    private String mode;

    /** 运行时间（秒） */
    @Schema(description = "运行时间（秒）")
    private Long uptimeSeconds;

    /** 已用内存（字节） */
    @Schema(description = "已用内存（字节）")
    private Long usedMemoryBytes;

    /** 内存上限（字节），0 表示未限制 */
    @Schema(description = "内存上限（字节），0 表示未限制")
    private Long maxmemoryBytes;

    /** 当前连接数 */
    @Schema(description = "当前连接数")
    private Long connectedClients;

    /** 每秒处理命令数 */
    @Schema(description = "每秒处理命令数")
    private Long instantaneousOps;

    /** 命中次数 */
    @Schema(description = "命中次数")
    private Long keyspaceHits;

    /** 未命中次数 */
    @Schema(description = "未命中次数")
    private Long keyspaceMisses;

    /** 当前库 Key 总数 */
    @Schema(description = "当前库 Key 总数")
    private Long totalKeyCount;
}
