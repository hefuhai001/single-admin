package com.hfh.api.modules.monitor.oss.config;

import com.hfh.api.modules.system.service.ConfigService;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * 动态 OSS 配置与客户端
 * <p>
 * 优先从 sys_config 系统配置读取，未配置时回退到 application-*.yaml 的 oss.* 属性。
 * </p>
 */
@Slf4j
@Configuration
@EnableConfigurationProperties
public class DynamicOssConfig {

    @Autowired
    private ConfigService configService;

    @Bean
    @ConfigurationProperties(prefix = "oss")
    public OssProperties yamlOssProperties() {
        return new OssProperties();
    }

    @Bean
    public OssProperties ossProperties() {
        OssProperties yaml = yamlOssProperties();
        OssProperties properties = new OssProperties();
        properties.setProvider(getConfigValue("oss.provider", yaml.getProvider()));
        properties.setEndpoint(getConfigValue("oss.endpoint", yaml.getEndpoint()));
        properties.setAccessKey(getConfigValue("oss.accessKey", yaml.getAccessKey()));
        properties.setSecretKey(getConfigValue("oss.secretKey", yaml.getSecretKey()));
        properties.setBucketName(getConfigValue("oss.bucket_name", yaml.getBucketName()));
        properties.setCustomDomain(getConfigValue("oss.custom_domain", yaml.getCustomDomain()));
        properties.setRegion(getConfigValue("oss.region", yaml.getRegion()));
        properties.setDomain(getConfigValue("oss.domain", yaml.getDomain()));
        log.info("OSS 配置加载：provider={}, endpoint={}, bucketName={}",
                properties.getProvider(), properties.getEndpoint(), properties.getBucketName());
        return properties;
    }

    @Bean
    public MinioClient minioClient() {
        OssProperties properties = ossProperties();
        String endpoint = getConfigValue("oss.endpoint", properties.getEndpoint());
        String accessKey = getConfigValue("oss.accessKey", properties.getAccessKey());
        String secretKey = getConfigValue("oss.secretKey", properties.getSecretKey());
        if (!StringUtils.hasText(endpoint) || !StringUtils.hasText(accessKey) || !StringUtils.hasText(secretKey)) {
            throw new RuntimeException("MinIO 配置不完整：endpoint/accessKey/secretKey 不能为空");
        }
        log.info("初始化 MinIO 客户端：endpoint={}", endpoint);
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    private String getConfigValue(String key, String defaultValue) {
        String value = configService.getConfigValue(key);
        return StringUtils.hasText(value) ? value : defaultValue;
    }
}
