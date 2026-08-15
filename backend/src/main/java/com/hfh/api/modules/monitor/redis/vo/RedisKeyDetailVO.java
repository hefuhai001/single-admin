package com.hfh.api.modules.monitor.redis.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Redis Key 详情
 */
@Data
@Schema(description = "Redis Key 详情")
public class RedisKeyDetailVO {

    /** Key 名称 */
    @Schema(description = "Key 名称")
    private String key;

    /** 类型（String/List/Set/Hash/ZSet） */
    @Schema(description = "类型")
    private String type;

    /** 剩余过期时间（秒），-1 表示永不过期 */
    @Schema(description = "剩余过期时间（秒），-1 表示永不过期")
    private Long ttl;

    /** 值长度 */
    @Schema(description = "值长度")
    private Long size;

    /** 值预览（仅 String 类型，取前 200 字符） */
    @Schema(description = "值预览（仅 String 类型，取前 200 字符）")
    private String valuePreview;
}
