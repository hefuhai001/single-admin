package com.hfh.api.modules.monitor.minio.service;

import org.springframework.web.multipart.MultipartFile;

import com.hfh.api.modules.monitor.minio.entity.vo.BucketVO;
import com.hfh.api.modules.monitor.minio.entity.vo.ObjectVO;
import com.hfh.api.modules.monitor.minio.entity.vo.UploadResultVO;

import java.io.InputStream;
import java.util.List;

/**
 * MinIO 文件存储服务
 */
public interface MinioService {

    /**
     * 列出所有存储桶
     */
    List<BucketVO> listBuckets();

    /**
     * 创建存储桶
     */
    void createBucket(String bucketName);

    /**
     * 删除存储桶（仅空桶可删除）
     */
    void removeBucket(String bucketName);

    /**
     * 列出指定存储桶下的对象
     */
    List<ObjectVO> listObjects(String bucketName, String prefix, boolean recursive);

    /**
     * 上传文件
     */
    UploadResultVO upload(MultipartFile file, String bucketName, String objectName);

    /**
     * 获取文件输入流
     */
    InputStream getObject(String bucketName, String objectName);

    /**
     * 获取文件临时预览 URL
     */
    String getPreviewUrl(String bucketName, String objectName, int expirySeconds);

    /**
     * 删除单个对象
     */
    void removeObject(String bucketName, String objectName);

    /**
     * 批量删除对象
     */
    void removeObjects(String bucketName, List<String> objectNames);
}
