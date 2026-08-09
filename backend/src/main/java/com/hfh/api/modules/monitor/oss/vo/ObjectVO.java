package com.hfh.api.modules.monitor.oss.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 对象/文件视图对象
 */
@Data
@Accessors(chain = true)
@Schema(name = "ObjectVO", description = "MinIO 对象信息")
public class ObjectVO {

    @Schema(description = "对象名称")
    private String objectName;

    @Schema(description = "对象大小（字节）")
    private Long size;

    @Schema(description = "最后修改时间")
    private Date lastModified;

    @Schema(description = "ETag")
    private String etag;

    @Schema(description = "内容类型")
    private String contentType;
}
