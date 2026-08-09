package com.hfh.api.modules.monitor.oss.enums;

/**
 * OSS 服务提供商枚举
 */
public enum OssProvider {

    MINIO("minio"),
    ALIYUN("aliyun"),
    QINIU("qiniu");

    private final String value;

    OssProvider(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static OssProvider from(String value) {
        if (value == null || value.isEmpty()) {
            return MINIO;
        }
        for (OssProvider provider : values()) {
            if (provider.value.equalsIgnoreCase(value)) {
                return provider;
            }
        }
        return MINIO;
    }
}
