package com.hfh.api.modules.monitor.redis.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Redis 命令统计项
 */
@Data
@Schema(description = "Redis 命令统计项")
public class RedisCommandStatsVO {

    /** 命令名（如 get/set） */
    @Schema(description = "命令名")
    private String command;

    /** 调用次数 */
    @Schema(description = "调用次数")
    private Long calls;

    /** 累计耗时（微秒） */
    @Schema(description = "累计耗时（微秒）")
    private Long usec;

    /** 单次平均耗时（微秒） */
    @Schema(description = "单次平均耗时（微秒）")
    private Double usecPerCall;
}
