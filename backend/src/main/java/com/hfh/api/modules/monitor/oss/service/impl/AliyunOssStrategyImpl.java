package com.hfh.api.modules.monitor.oss.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ListObjectsRequest;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.OSSObjectSummary;
import com.aliyun.oss.model.ObjectListing;
import com.hfh.api.modules.monitor.oss.config.OssProperties;
import com.hfh.api.modules.monitor.oss.enums.OssProvider;
import com.hfh.api.modules.monitor.oss.service.OssStrategy;
import com.hfh.api.modules.monitor.oss.service.support.AbstractOssStrategy;
import com.hfh.api.modules.monitor.oss.vo.BucketVO;
import com.hfh.api.modules.monitor.oss.vo.ObjectVO;
import com.hfh.api.modules.monitor.oss.vo.UploadResultVO;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 阿里云 OSS 策略实现
 */
@Service
public class AliyunOssStrategyImpl extends AbstractOssStrategy implements OssStrategy {

    public AliyunOssStrategyImpl(OssProperties ossProperties) {
        super(ossProperties);
    }

    private OSS buildClient() {
        String endpoint = ossProperties.getEndpoint();
        if (!StringUtils.hasText(endpoint)) {
            throw new RuntimeException("阿里云 OSS endpoint 未配置");
        }
        return new OSSClientBuilder().build(endpoint, ossProperties.getAccessKey(), ossProperties.getSecretKey());
    }

    @Override
    public OssProvider getProvider() {
        return OssProvider.ALIYUN;
    }

    @Override
    public List<BucketVO> listBuckets() {
        OSS client = null;
        try {
            client = buildClient();
            List<BucketVO> list = new ArrayList<>();
            for (com.aliyun.oss.model.Bucket bucket : client.listBuckets()) {
                BucketVO vo = new BucketVO();
                vo.setName(bucket.getName());
                vo.setCreationDate(bucket.getCreationDate());
                list.add(vo);
            }
            return list;
        } catch (Exception e) {
            throw new RuntimeException("查询存储桶失败：" + e.getMessage(), e);
        } finally {
            if (client != null) {
                client.shutdown();
            }
        }
    }

    @Override
    public void createBucket(String bucketName) {
        validateBucketName(bucketName);
        OSS client = null;
        try {
            client = buildClient();
            if (client.doesBucketExist(bucketName)) {
                throw new RuntimeException("存储桶已存在");
            }
            client.createBucket(bucketName);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("创建存储桶失败：" + e.getMessage(), e);
        } finally {
            if (client != null) {
                client.shutdown();
            }
        }
    }

    @Override
    public void removeBucket(String bucketName) {
        validateBucketName(bucketName);
        OSS client = null;
        try {
            client = buildClient();
            client.deleteBucket(bucketName);
        } catch (Exception e) {
            throw new RuntimeException("删除存储桶失败，请确认存储桶为空：" + e.getMessage(), e);
        } finally {
            if (client != null) {
                client.shutdown();
            }
        }
    }

    @Override
    public List<ObjectVO> listObjects(String bucketName, String prefix, boolean recursive) {
        validateBucketName(bucketName);
        OSS client = null;
        try {
            client = buildClient();
            ListObjectsRequest request = new ListObjectsRequest(bucketName);
            if (StringUtils.hasText(prefix)) {
                request.setPrefix(prefix);
            }
            request.setMaxKeys(1000);
            ObjectListing listing = client.listObjects(request);
            List<ObjectVO> list = new ArrayList<>();
            for (OSSObjectSummary summary : listing.getObjectSummaries()) {
                ObjectVO vo = new ObjectVO();
                vo.setObjectName(summary.getKey());
                vo.setSize(summary.getSize());
                vo.setLastModified(summary.getLastModified());
                vo.setEtag(summary.getETag());
                list.add(vo);
            }
            return list;
        } catch (Exception e) {
            throw new RuntimeException("查询对象列表失败：" + e.getMessage(), e);
        } finally {
            if (client != null) {
                client.shutdown();
            }
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

        String contentType = StringUtils.hasText(file.getContentType())
                ? file.getContentType()
                : MediaType.APPLICATION_OCTET_STREAM_VALUE;

        OSS client = null;
        try (InputStream inputStream = file.getInputStream()) {
            client = buildClient();
            if (!client.doesBucketExist(targetBucket)) {
                client.createBucket(targetBucket);
            }
            com.aliyun.oss.model.ObjectMetadata metadata = new com.aliyun.oss.model.ObjectMetadata();
            metadata.setContentType(contentType);
            metadata.setContentLength(file.getSize());
            client.putObject(targetBucket, targetObject, inputStream, metadata);
            OSSObject ossObject = client.getObject(targetBucket, targetObject);

            UploadResultVO result = new UploadResultVO();
            result.setBucket(targetBucket);
            result.setObjectName(targetObject);
            result.setEtag(ossObject.getObjectMetadata().getETag());
            result.setUrl(getPreviewUrl(targetBucket, targetObject, 3600));
            result.setPermanentUrl(buildPermanentUrl(targetBucket, targetObject));
            return result;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("文件上传失败：" + e.getMessage(), e);
        } finally {
            if (client != null) {
                client.shutdown();
            }
        }
    }

    @Override
    public InputStream getObject(String bucketName, String objectName) {
        validateBucketName(bucketName);
        validateObjectName(objectName);
        try {
            OSS client = buildClient();
            OSSObject ossObject = client.getObject(bucketName, objectName);
            return ossObject.getObjectContent();
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
        OSS client = null;
        try {
            client = buildClient();
            Date expiration = new Date(System.currentTimeMillis() + expirySeconds * 1000L);
            return client.generatePresignedUrl(bucketName, objectName, expiration).toString();
        } catch (Exception e) {
            throw new RuntimeException("生成预览地址失败：" + e.getMessage(), e);
        } finally {
            if (client != null) {
                client.shutdown();
            }
        }
    }

    @Override
    public void removeObject(String bucketName, String objectName) {
        validateBucketName(bucketName);
        validateObjectName(objectName);
        OSS client = null;
        try {
            client = buildClient();
            client.deleteObject(bucketName, objectName);
        } catch (Exception e) {
            throw new RuntimeException("删除对象失败：" + e.getMessage(), e);
        } finally {
            if (client != null) {
                client.shutdown();
            }
        }
    }

    @Override
    public void removeObjects(String bucketName, List<String> objectNames) {
        validateBucketName(bucketName);
        if (objectNames == null || objectNames.isEmpty()) {
            return;
        }
        objectNames.forEach(this::validateObjectName);
        OSS client = null;
        try {
            client = buildClient();
            client.deleteObjects(new com.aliyun.oss.model.DeleteObjectsRequest(bucketName).withKeys(objectNames));
        } catch (Exception e) {
            throw new RuntimeException("批量删除对象失败：" + e.getMessage(), e);
        } finally {
            if (client != null) {
                client.shutdown();
            }
        }
    }
}
