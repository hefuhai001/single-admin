package com.hfh.api.modules.monitor.redis.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * Redis Key 树节点
 * <p>Key 按冒号分段构建命名空间树，如 config:key:xxx → config / key / xxx</p>
 */
@Data
@Schema(description = "Redis Key 树节点")
public class RedisKeyTreeVO {

    /** 节点名称（当前分段） */
    @Schema(description = "节点名称（当前分段）")
    private String label;

    /** 完整前缀（从根到当前节点的冒号拼接），空串表示根 */
    @Schema(description = "完整前缀，空串表示根")
    private String prefix;

    /** 完整 Key 名（仅当该节点本身是真实 Key 时非空，如 config 既是命名空间又是独立 Key） */
    @Schema(description = "完整 Key 名，仅当该节点本身是真实 Key 时非空")
    private String fullKey;

    /** 该前缀下的 Key 总数（含子孙） */
    @Schema(description = "该前缀下的 Key 总数")
    private Long count;

    /** 是否为叶子节点（无子命名空间） */
    @Schema(description = "是否为叶子节点")
    private Boolean leaf;

    /** 子节点 */
    @Schema(description = "子节点")
    private List<RedisKeyTreeVO> children;
}
