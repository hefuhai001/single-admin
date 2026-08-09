package com.hfh.api.modules.monitor.oss.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 文件上传结果视图对象
 */
@Data
@Accessors(chain = true)
@Schema(name = "UploadResultVO", description = "文件上传结果")
public class UploadResultVO {

    @Schema(description = "存储桶名称")
    private String bucket;

    @Schema(description = "对象名称")
    private String objectName;

    @Schema(description = "ETag")
    private String etag;

    @Schema(description = "访问地址（短期预签名）")
    private String url;

    @Schema(description = "长期有效访问地址（自定义域名 + 桶 + 文件名）")
    private String permanentUrl;
}
