package com.hfh.api.modules.monitor.oss.service.support;

import com.hfh.api.modules.monitor.oss.config.OssProperties;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.regex.Pattern;

/**
 * OSS 策略公共逻辑
 */
public abstract class AbstractOssStrategy {

    protected final OssProperties ossProperties;

    /**
     * 存储桶名称规则：小写字母、数字、中划线，3-63 字符，不能以中划线开头或结尾
     */
    private static final Pattern BUCKET_NAME_PATTERN = Pattern.compile("^[a-z0-9][a-z0-9-]{1,61}[a-z0-9]$");

    protected AbstractOssStrategy(OssProperties ossProperties) {
        this.ossProperties = ossProperties;
    }

    protected String defaultBucketName(String bucketName) {
        return StringUtils.hasText(bucketName) ? bucketName : ossProperties.getBucketName();
    }

    protected String defaultObjectName(MultipartFile file, String objectName) {
        String targetObject = StringUtils.hasText(objectName) ? objectName : file.getOriginalFilename();
        if (!StringUtils.hasText(targetObject)) {
            throw new IllegalArgumentException("对象名称不能为空");
        }
        return targetObject;
    }

    public void validateBucketName(String bucketName) {
        if (!StringUtils.hasText(bucketName) || !BUCKET_NAME_PATTERN.matcher(bucketName).matches()) {
            throw new IllegalArgumentException("存储桶名称不合法：" + bucketName);
        }
    }

    public void validateObjectName(String objectName) {
        if (!StringUtils.hasText(objectName)
                || objectName.contains("..")
                || objectName.contains("\\")
                || objectName.startsWith("/")) {
            throw new IllegalArgumentException("对象名称不合法：" + objectName);
        }
    }

    public String buildPermanentUrl(String bucketName, String objectName) {
        String domain = StringUtils.hasText(ossProperties.getCustomDomain())
                ? ossProperties.getCustomDomain()
                : ossProperties.getEndpoint();
        if (!StringUtils.hasText(domain)) {
            return "";
        }
        if (!domain.endsWith("/")) {
            domain = domain + "/";
        }
        return domain + bucketName + "/" + objectName;
    }
}
