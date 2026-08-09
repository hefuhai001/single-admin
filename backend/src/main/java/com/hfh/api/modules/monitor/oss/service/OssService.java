package com.hfh.api.modules.monitor.oss.service;

import com.hfh.api.modules.monitor.oss.config.OssProperties;
import com.hfh.api.modules.monitor.oss.enums.OssProvider;
import com.hfh.api.modules.monitor.oss.vo.BucketVO;
import com.hfh.api.modules.monitor.oss.vo.ObjectVO;
import com.hfh.api.modules.monitor.oss.vo.UploadResultVO;
import com.hfh.api.modules.system.service.ConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

/**
 * OSS 路由服务（根据 sys_config 实时 provider 选择对应策略）
 */
@Slf4j
@Service
public class OssService implements com.hfh.api.modules.monitor.oss.service.OssStrategy {

    private final OssProperties ossProperties;
    private final ConfigService configService;
    private final List<OssStrategy> strategies;

    public OssService(OssProperties ossProperties, ConfigService configService, List<OssStrategy> strategies) {
        this.ossProperties = ossProperties;
        this.configService = configService;
        this.strategies = strategies;
    }

    private String currentProviderValue() {
        String provider = configService.getConfigValue("oss.provider");
        if (!StringUtils.hasText(provider)) {
            provider = ossProperties.getProvider();
        }
        return provider;
    }

    private OssStrategy current() {
        OssProvider provider = OssProvider.from(currentProviderValue());
        log.debug("当前 OSS provider：{}", provider.getValue());
        for (OssStrategy strategy : strategies) {
            if (strategy.getProvider() == provider) {
                return strategy;
            }
        }
        throw new RuntimeException("不支持的 OSS 提供商：" + provider.getValue());
    }

    @Override
    public OssProvider getProvider() {
        return OssProvider.from(currentProviderValue());
    }

    @Override
    public List<BucketVO> listBuckets() {
        return current().listBuckets();
    }

    @Override
    public void createBucket(String bucketName) {
        current().createBucket(bucketName);
    }

    @Override
    public void removeBucket(String bucketName) {
        current().removeBucket(bucketName);
    }

    @Override
    public List<ObjectVO> listObjects(String bucketName, String prefix, boolean recursive) {
        return current().listObjects(bucketName, prefix, recursive);
    }

    @Override
    public UploadResultVO upload(MultipartFile file, String bucketName, String objectName) {
        return current().upload(file, bucketName, objectName);
    }

    @Override
    public InputStream getObject(String bucketName, String objectName) {
        return current().getObject(bucketName, objectName);
    }

    @Override
    public String getPreviewUrl(String bucketName, String objectName, int expirySeconds) {
        return current().getPreviewUrl(bucketName, objectName, expirySeconds);
    }

    @Override
    public void removeObject(String bucketName, String objectName) {
        current().removeObject(bucketName, objectName);
    }

    @Override
    public void removeObjects(String bucketName, List<String> objectNames) {
        current().removeObjects(bucketName, objectNames);
    }
}
