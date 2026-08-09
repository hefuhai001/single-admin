package com.hfh.api.modules.monitor.oss.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 存储桶视图对象
 */
@Data
@Accessors(chain = true)
@Schema(name = "BucketVO", description = "存储桶信息")
public class BucketVO {

    @Schema(description = "存储桶名称")
    private String name;

    @Schema(description = "创建时间")
    private Date creationDate;
}
