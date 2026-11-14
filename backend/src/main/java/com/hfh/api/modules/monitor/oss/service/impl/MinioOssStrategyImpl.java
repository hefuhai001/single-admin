package com.hfh.api.modules.monitor.oss.service.impl;

import com.hfh.api.modules.monitor.oss.config.OssProperties;
import com.hfh.api.modules.monitor.oss.enums.OssProvider;
import com.hfh.api.modules.monitor.oss.service.OssStrategy;
import com.hfh.api.modules.monitor.oss.service.support.AbstractOssStrategy;
import com.hfh.api.modules.monitor.oss.vo.BucketVO;
import com.hfh.api.modules.monitor.oss.vo.ObjectVO;
import com.hfh.api.modules.monitor.oss.vo.UploadResultVO;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.DeleteError;
import io.minio.messages.Item;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MinIO 策略实现
 */
@Service
public class MinioOssStrategyImpl extends AbstractOssStrategy implements OssStrategy {

    private final ObjectProvider<io.minio.MinioClient> minioClientProvider;

    public MinioOssStrategyImpl(OssProperties ossProperties, ObjectProvider<io.minio.MinioClient> minioClientProvider) {
        super(ossProperties);
        this.minioClientProvider = minioClientProvider;
    }

    private io.minio.MinioClient getClient() {
        return minioClientProvider.getIfAvailable(() -> {
            throw new RuntimeException("MinIO 客户端未配置，请确认 oss.provider=minio");
        });
    }

    @Override
    public OssProvider getProvider() {
        return OssProvider.MINIO;
    }

    @Override
    public List<BucketVO> listBuckets() {
        try {
            return getClient().listBuckets().stream()
                    .map(bucket -> new BucketVO()
                            .setName(bucket.name())
                            .setCreationDate(Date.from(bucket.creationDate().toInstant())))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("查询存储桶失败：" + e.getMessage(), e);
        }
    }

    @Override
    public void createBucket(String bucketName) {
        validateBucketName(bucketName);
        try {
            io.minio.MinioClient client = getClient();
            boolean exists = client.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (exists) {
                throw new RuntimeException("存储桶已存在");
            }
            client.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
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
            getClient().removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            throw new RuntimeException("删除存储桶失败，请确认存储桶为空：" + e.getMessage(), e);
        }
    }

    @Override
    public List<ObjectVO> listObjects(String bucketName, String prefix, boolean recursive) {
        validateBucketName(bucketName);
        try {
            ListObjectsArgs.Builder builder = ListObjectsArgs.builder()
                    .bucket(bucketName)
                    .recursive(recursive);
            if (StringUtils.hasText(prefix)) {
                builder.prefix(prefix);
            }
            List<ObjectVO> list = new ArrayList<>();
            for (io.minio.Result<Item> result : getClient().listObjects(builder.build())) {
                if (list.size() >= MAX_LIST_OBJECTS) {
                    break;
                }
                Item item = result.get();
                ObjectVO vo = new ObjectVO();
                vo.setObjectName(item.objectName());
                vo.setSize(item.size());
                vo.setLastModified(Date.from(item.lastModified().toInstant()));
                vo.setEtag(item.etag());
                list.add(vo);
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
        ensureBucketExists(targetBucket);

        String targetObject = defaultObjectName(file, objectName);
        validateObjectName(targetObject);

        String contentType = StringUtils.hasText(file.getContentType())
                ? file.getContentType()
                : MediaType.APPLICATION_OCTET_STREAM_VALUE;

        io.minio.MinioClient client = getClient();
        try (InputStream inputStream = file.getInputStream()) {
            client.putObject(
                    PutObjectArgs.builder()
                            .bucket(targetBucket)
                            .object(targetObject)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(contentType)
                            .build());

            StatObjectResponse stat = client.statObject(
                    StatObjectArgs.builder().bucket(targetBucket).object(targetObject).build());

            UploadResultVO result = new UploadResultVO();
            result.setBucket(targetBucket);
            result.setObjectName(targetObject);
            result.setEtag(stat.etag());
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
            return getClient().getObject(GetObjectArgs.builder().bucket(bucketName).object(objectName).build());
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
        try {
            return getClient().getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(expirySeconds)
                            .build());
        } catch (Exception e) {
            throw new RuntimeException("生成预览地址失败：" + e.getMessage(), e);
        }
    }

    @Override
    public void removeObject(String bucketName, String objectName) {
        validateBucketName(bucketName);
        validateObjectName(objectName);
        try {
            getClient().removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(objectName).build());
        } catch (Exception e) {
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

        List<io.minio.messages.DeleteObject> deleteObjects = objectNames.stream()
                .map(io.minio.messages.DeleteObject::new)
                .collect(Collectors.toList());

        try {
            Iterable<io.minio.Result<DeleteError>> results = getClient().removeObjects(
                    RemoveObjectsArgs.builder().bucket(bucketName).objects(deleteObjects).build());

            List<String> errors = new ArrayList<>();
            for (io.minio.Result<DeleteError> result : results) {
                try {
                    DeleteError error = result.get();
                    errors.add(error.objectName() + ":" + error.message());
                } catch (Exception ignored) {
                }
            }
            if (!errors.isEmpty()) {
                throw new RuntimeException("部分对象删除失败：" + String.join(", ", errors));
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("批量删除对象失败：" + e.getMessage(), e);
        }
    }

    private void ensureBucketExists(String bucketName) {
        try {
            io.minio.MinioClient client = getClient();
            boolean exists = client.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!exists) {
                client.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
        } catch (Exception e) {
            throw new RuntimeException("检查/创建存储桶失败：" + e.getMessage(), e);
        }
    }
}
