package com.hfh.api.modules.monitor.oss.service.impl;

import com.hfh.api.modules.monitor.oss.config.OssProperties;
import com.hfh.api.modules.monitor.oss.enums.OssProvider;
import com.hfh.api.modules.monitor.oss.service.OssStrategy;
import com.hfh.api.modules.monitor.oss.service.support.AbstractOssStrategy;
import com.hfh.api.modules.monitor.oss.vo.BucketVO;
import com.hfh.api.modules.monitor.oss.vo.ObjectVO;
import com.hfh.api.modules.monitor.oss.vo.UploadResultVO;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.FileInfo;
import com.qiniu.util.Auth;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 七牛云 OSS 策略实现
 */
@Service
public class QiniuOssStrategyImpl extends AbstractOssStrategy implements OssStrategy {

    public QiniuOssStrategyImpl(OssProperties ossProperties) {
        super(ossProperties);
    }

    private Auth buildAuth() {
        return Auth.create(ossProperties.getAccessKey(), ossProperties.getSecretKey());
    }

    private Region resolveRegion() {
        if (StringUtils.hasText(ossProperties.getRegion())) {
            String region = ossProperties.getRegion();
            return switch (region) {
                case "z0", "huadong" -> Region.huadong();
                case "z1", "huabei" -> Region.huabei();
                case "z2", "huanan" -> Region.huanan();
                case "na0", "beimei" -> Region.beimei();
                case "as0", "xinjiapo" -> Region.xinjiapo();
                case "cn-east-2", "huadongZheJiang2" -> Region.huadongZheJiang2();
                default -> Region.autoRegion();
            };
        }
        return Region.autoRegion();
    }

    private String regionId() {
        if (StringUtils.hasText(ossProperties.getRegion())) {
            return ossProperties.getRegion();
        }
        return "z0";
    }

    private Configuration buildConfiguration() {
        return new Configuration(resolveRegion());
    }

    private UploadManager uploadManager() {
        return new UploadManager(buildConfiguration());
    }

    private BucketManager bucketManager() {
        return new BucketManager(buildAuth(), buildConfiguration());
    }

    private String domain() {
        String domain = StringUtils.hasText(ossProperties.getCustomDomain())
                ? ossProperties.getCustomDomain()
                : ossProperties.getDomain();
        if (!StringUtils.hasText(domain)) {
            throw new RuntimeException("七牛云域名未配置");
        }
        if (!domain.endsWith("/")) {
            domain = domain + "/";
        }
        return domain;
    }

    @Override
    public OssProvider getProvider() {
        return OssProvider.QINIU;
    }

    @Override
    public List<BucketVO> listBuckets() {
        try {
            BucketManager manager = bucketManager();
            List<BucketVO> list = new ArrayList<>();
            for (String bucket : manager.buckets()) {
                BucketVO vo = new BucketVO();
                vo.setName(bucket);
                list.add(vo);
            }
            return list;
        } catch (Exception e) {
            throw new RuntimeException("查询存储桶失败：" + e.getMessage(), e);
        }
    }

    @Override
    public void createBucket(String bucketName) {
        validateBucketName(bucketName);
        try {
            BucketManager manager = bucketManager();
            String[] buckets = manager.buckets();
            for (String b : buckets) {
                if (b.equals(bucketName)) {
                    throw new RuntimeException("存储桶已存在");
                }
            }
            manager.createBucket(bucketName, regionId());
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("创建存储桶失败：" + e.getMessage(), e);
        }
    }

    @Override
    public void removeBucket(String bucketName) {
        validateBucketName(bucketName);
        try {
            bucketManager().delete(bucketName, null);
        } catch (Exception e) {
            throw new RuntimeException("删除存储桶失败，请确认存储桶为空：" + e.getMessage(), e);
        }
    }

    @Override
    public List<ObjectVO> listObjects(String bucketName, String prefix, boolean recursive) {
        validateBucketName(bucketName);
        try {
            BucketManager manager = bucketManager();
            List<ObjectVO> list = new ArrayList<>();
            BucketManager.FileListIterator iterator = manager.createFileListIterator(bucketName, prefix, 1000, null);
            while (iterator.hasNext()) {
                FileInfo[] items = iterator.next();
                for (FileInfo item : items) {
                    if (list.size() >= MAX_LIST_OBJECTS) {
                        return list;
                    }
                    ObjectVO vo = new ObjectVO();
                    vo.setObjectName(item.key);
                    vo.setSize(item.fsize);
                    vo.setLastModified(new Date(item.putTime / 10000));
                    vo.setEtag(item.hash);
                    list.add(vo);
                }
            }
            return list;
        } catch (Exception e) {
            throw new RuntimeException("查询对象列表失败：" + e.getMessage(), e);
        }
    }

    @Override
    public UploadResultVO upload(MultipartFile file, String bucketName, String objectName) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        String targetBucket = defaultBucketName(bucketName);
        validateBucketName(targetBucket);
        String targetObject = defaultObjectName(file, objectName);
        validateObjectName(targetObject);

        try (InputStream inputStream = file.getInputStream()) {
            byte[] bytes = inputStream.readAllBytes();
            Auth auth = buildAuth();
            String upToken = auth.uploadToken(targetBucket);
            UploadManager uploadManager = uploadManager();
            Response response = uploadManager.put(bytes, targetObject, upToken);
            if (!response.isOK()) {
                throw new RuntimeException("上传失败：" + response.error);
            }
            com.qiniu.storage.model.DefaultPutRet putRet = response.jsonToObject(com.qiniu.storage.model.DefaultPutRet.class);

            UploadResultVO result = new UploadResultVO();
            result.setBucket(targetBucket);
            result.setObjectName(targetObject);
            result.setEtag(putRet.hash);
            result.setUrl(getPreviewUrl(targetBucket, targetObject, 3600));
            result.setPermanentUrl(buildPermanentUrl(targetBucket, targetObject));
            return result;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("文件上传失败：" + e.getMessage(), e);
        }
    }

    @Override
    public InputStream getObject(String bucketName, String objectName) {
        validateBucketName(bucketName);
        validateObjectName(objectName);
        try {
            String url = buildAuth().privateDownloadUrl(domain() + objectName, 3600);
            java.net.URL netUrl = new java.net.URL(url);
            return netUrl.openStream();
        } catch (Exception e) {
            throw new RuntimeException("获取文件失败：" + e.getMessage(), e);
        }
    }

    @Override
    public String getPreviewUrl(String bucketName, String objectName, int expirySeconds) {
        validateBucketName(bucketName);
        validateObjectName(objectName);
        if (expirySeconds <= 0) {
            expirySeconds = 3600;
        }
        return buildAuth().privateDownloadUrl(domain() + objectName, expirySeconds);
    }

    @Override
    public void removeObject(String bucketName, String objectName) {
        validateBucketName(bucketName);
        validateObjectName(objectName);
        try {
            bucketManager().delete(bucketName, objectName);
        } catch (QiniuException e) {
            throw new RuntimeException("删除对象失败：" + e.getMessage(), e);
        }
    }

    @Override
    public void removeObjects(String bucketName, List<String> objectNames) {
        validateBucketName(bucketName);
        if (objectNames == null || objectNames.isEmpty()) {
            return;
        }
        objectNames.forEach(this::validateObjectName);
        try {
            BucketManager manager = bucketManager();
            for (String objectName : objectNames) {
                manager.delete(bucketName, objectName);
            }
        } catch (QiniuException e) {
            throw new RuntimeException("批量删除对象失败：" + e.getMessage(), e);
        }
    }

    @Override
    public String buildPermanentUrl(String bucketName, String objectName) {
        return domain() + objectName;
    }
}
