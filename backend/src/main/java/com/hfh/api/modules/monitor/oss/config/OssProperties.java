package com.hfh.api.modules.monitor.oss.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * OSS YAML 配置属性（仅由 DynamicOssConfig 读取并合并 sys_config 后注册为唯一 OssProperties bean）
 */
@Data
@ConfigurationProperties(prefix = "oss")
public class OssProperties {

    /**
     * 服务提供商：minio / aliyun / qiniu
     */
    private String provider = "minio";

    /**
     * OSS 服务端点
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

    /**
     * 阿里云 OSS 区域（如 cn-hangzhou），endpoint 为空时自动构造
     */
    private String region;

    /**
     * 七牛云域名（bucket 外部访问域名，如 http://xxx.xxx.com）
     */
    private String domain;
}
