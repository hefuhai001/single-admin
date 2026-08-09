package com.hfh.api.modules.monitor.oss.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * OSS 配置属性
 */
@Data
@ConfigurationProperties(prefix = "oss")
public class OssProperties {

    /**
     * OSS 服务地址
     */
    private String endpoint;

    /**
     * 访问密钥
     */
    private String accessKey;

    /**
     * 秘密密钥
     */
    private String secretKey;

    /**
     * 默认存储桶
     */
    private String bucketName;

    /**
     * 文件自定义访问域名（为空则使用 endpoint）
     */
    private String customDomain;
}
