package com.hfh.api.modules.monitor.minio.service.impl;

import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.DeleteError;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.hfh.api.modules.monitor.minio.config.MinioProperties;
import com.hfh.api.modules.monitor.minio.service.MinioService;
import com.hfh.api.modules.monitor.minio.vo.BucketVO;
import com.hfh.api.modules.monitor.minio.vo.ObjectVO;
import com.hfh.api.modules.monitor.minio.vo.UploadResultVO;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * MinIO 服务实现
 */
@Service
@RequiredArgsConstructor
public class MinioServiceImpl implements MinioService {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    /**
     * 存储桶名称规则：小写字母、数字、中划线，3-63 字符，不能以中划线开头或结尾
     */
    private static final Pattern BUCKET_NAME_PATTERN = Pattern.compile("^[a-z0-9][a-z0-9-]{1,61}[a-z0-9]$");

    @Override
    public List<BucketVO> listBuckets() {
        try {
            return minioClient.listBuckets().stream()
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
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build());
            if (exists) {
                throw new RuntimeException("存储桶已存在");
            }
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
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
            minioClient.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
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
            for (io.minio.Result<Item> result : minioClient.listObjects(builder.build())) {
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
        String targetBucket = StringUtils.hasText(bucketName) ? bucketName : minioProperties.getBucketName();
        validateBucketName(targetBucket);
        ensureBucketExists(targetBucket);

        String targetObject = StringUtils.hasText(objectName) ? objectName : file.getOriginalFilename();
        if (!StringUtils.hasText(targetObject)) {
            throw new IllegalArgumentException("对象名称不能为空");
        }
        validateObjectName(targetObject);

        String contentType = StringUtils.hasText(file.getContentType())
                ? file.getContentType()
                : MediaType.APPLICATION_OCTET_STREAM_VALUE;

        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(targetBucket)
                            .object(targetObject)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(contentType)
                            .build());

            StatObjectResponse stat = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(targetBucket)
                            .object(targetObject)
                            .build());

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
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build());
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
            return minioClient.getPresignedObjectUrl(
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
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build());
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
            Iterable<io.minio.Result<DeleteError>> results = minioClient.removeObjects(
                    RemoveObjectsArgs.builder()
                            .bucket(bucketName)
                            .objects(deleteObjects)
                            .build());

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

    /**
     * 如果存储桶不存在则自动创建
     */
    private void ensureBucketExists(String bucketName) {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
        } catch (Exception e) {
            throw new RuntimeException("检查/创建存储桶失败：" + e.getMessage(), e);
        }
    }

    private void validateBucketName(String bucketName) {
        if (!StringUtils.hasText(bucketName) || !BUCKET_NAME_PATTERN.matcher(bucketName).matches()) {
            throw new IllegalArgumentException("存储桶名称不合法：" + bucketName);
        }
    }

    private void validateObjectName(String objectName) {
        if (!StringUtils.hasText(objectName)
                || objectName.contains("..")
                || objectName.contains("\\")
                || objectName.startsWith("/")) {
            throw new IllegalArgumentException("对象名称不合法：" + objectName);
        }
    }

    private String buildPermanentUrl(String bucketName, String objectName) {
        String domain = StringUtils.hasText(minioProperties.getCustomDomain())
                ? minioProperties.getCustomDomain()
                : minioProperties.getEndpoint();
        if (!domain.endsWith("/")) {
            domain = domain + "/";
        }
        return domain + bucketName + "/" + objectName;
    }
}
