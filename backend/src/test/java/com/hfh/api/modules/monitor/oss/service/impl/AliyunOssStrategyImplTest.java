package com.hfh.api.modules.monitor.oss.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSErrorCode;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.Bucket;
import com.aliyun.oss.model.DeleteObjectsRequest;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.OSSObjectSummary;
import com.aliyun.oss.model.ObjectListing;
import com.aliyun.oss.model.ObjectMetadata;
import com.hfh.api.modules.monitor.oss.config.OssProperties;
import com.hfh.api.modules.monitor.oss.enums.OssProvider;
import com.hfh.api.modules.monitor.oss.vo.BucketVO;
import com.hfh.api.modules.monitor.oss.vo.ObjectVO;
import com.hfh.api.modules.monitor.oss.vo.UploadResultVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AliyunOssStrategyImplTest {

    private OssProperties properties;
    private OSS client;
    private AliyunOssStrategyImpl strategy;
    private MockedConstruction<OSSClientBuilder> construction;

    @BeforeEach
    void setUp() {
        properties = new OssProperties();
        properties.setEndpoint("oss-cn-hangzhou.aliyuncs.com");
        properties.setAccessKey("ak");
        properties.setSecretKey("sk");
        properties.setBucketName("default-bucket");
        properties.setCustomDomain("http://cdn.example.com");
        client = mock(OSS.class);
        strategy = new AliyunOssStrategyImpl(properties);
        construction = mockConstruction(OSSClientBuilder.class,
                (mockBuilder, context) -> when(mockBuilder.build(anyString(), anyString(), anyString()))
                        .thenReturn(client));
    }

    @AfterEach
    void tearDown() {
        construction.close();
    }

    @Test
    void providerShouldBeAliyun() {
        assertThat(strategy.getProvider()).isEqualTo(OssProvider.ALIYUN);
    }

    @Test
    void listBucketsShouldMapAndShutdown() {
        Bucket bucket = mock(Bucket.class);
        Date date = new Date();
        when(bucket.getName()).thenReturn("b1");
        when(bucket.getCreationDate()).thenReturn(date);
        when(client.listBuckets()).thenReturn(List.of(bucket));

        List<BucketVO> list = strategy.listBuckets();

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getName()).isEqualTo("b1");
        assertThat(list.get(0).getCreationDate()).isEqualTo(date);
        verify(client).shutdown();
    }

    @Test
    void listBucketsShouldWrapFailure() {
        when(client.listBuckets()).thenThrow(new RuntimeException("oss down"));

        assertThatThrownBy(() -> strategy.listBuckets())
                .hasMessageContaining("查询存储桶失败");
        verify(client).shutdown();
    }

    @Test
    void createBucketShouldRejectExisting() {
        when(client.doesBucketExist("bucket1")).thenReturn(true);

        assertThatThrownBy(() -> strategy.createBucket("bucket1"))
                .hasMessage("存储桶已存在");
        verify(client, never()).createBucket(anyString());
    }

    @Test
    void createBucketShouldCreateWhenMissing() {
        when(client.doesBucketExist("bucket1")).thenReturn(false);

        strategy.createBucket("bucket1");

        verify(client).createBucket("bucket1");
        verify(client).shutdown();
    }

    @Test
    void createBucketShouldValidateNameAndEndpoint() {
        assertThatThrownBy(() -> strategy.createBucket("Bad_Name")).isInstanceOf(IllegalArgumentException.class);

        properties.setEndpoint("");
        assertThatThrownBy(() -> strategy.createBucket("bucket1"))
                .hasMessageContaining("endpoint 未配置");
    }

    @Test
    void removeBucketShouldDelete() {
        strategy.removeBucket("bucket1");

        verify(client).deleteBucket("bucket1");
        verify(client).shutdown();
    }

    @Test
    void removeBucketShouldWrapFailure() {
        doThrow(new OSSException("x")).when(client).deleteBucket("bucket1");

        assertThatThrownBy(() -> strategy.removeBucket("bucket1"))
                .hasMessageContaining("删除存储桶失败");
    }

    @Test
    void listObjectsShouldMapSummaries() {
        OSSObjectSummary summary = mock(OSSObjectSummary.class);
        when(summary.getKey()).thenReturn("a.txt");
        when(summary.getSize()).thenReturn(12L);
        when(summary.getETag()).thenReturn("etag");
        ObjectListing listing = mock(ObjectListing.class);
        when(listing.getObjectSummaries()).thenReturn(List.of(summary));
        when(client.listObjects(any(com.aliyun.oss.model.ListObjectsRequest.class))).thenReturn(listing);

        List<ObjectVO> list = strategy.listObjects("bucket1", "prefix", false);

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getObjectName()).isEqualTo("a.txt");
        assertThat(list.get(0).getSize()).isEqualTo(12L);
        assertThat(list.get(0).getEtag()).isEqualTo("etag");
    }

    @Test
    void listObjectsShouldWrapFailure() {
        when(client.listObjects(any(com.aliyun.oss.model.ListObjectsRequest.class)))
                .thenThrow(new RuntimeException("boom"));

        assertThatThrownBy(() -> strategy.listObjects("bucket1", null, true))
                .hasMessageContaining("查询对象列表失败");
    }

    @Test
    void uploadShouldReturnResult() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("a.txt");
        when(file.getContentType()).thenReturn(null);
        when(file.getSize()).thenReturn(3L);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[]{1, 2, 3}));
        when(client.doesBucketExist("bucket1")).thenReturn(false);
        OSSObject ossObject = mock(OSSObject.class);
        ObjectMetadata metadata = mock(ObjectMetadata.class);
        when(metadata.getETag()).thenReturn("etag-1");
        when(ossObject.getObjectMetadata()).thenReturn(metadata);
        when(client.getObject("bucket1", "a.txt")).thenReturn(ossObject);
        when(client.generatePresignedUrl(eq("bucket1"), eq("a.txt"), any(Date.class)))
                .thenReturn(new URL("https://example.com/a.txt"));

        UploadResultVO result = strategy.upload(file, "bucket1", null);

        assertThat(result.getBucket()).isEqualTo("bucket1");
        assertThat(result.getObjectName()).isEqualTo("a.txt");
        assertThat(result.getEtag()).isEqualTo("etag-1");
        assertThat(result.getUrl()).isEqualTo("https://example.com/a.txt");
        assertThat(result.getPermanentUrl()).isEqualTo("http://cdn.example.com/bucket1/a.txt");
        verify(client).createBucket("bucket1");
    }

    @Test
    void uploadShouldRejectEmptyFile() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        assertThatThrownBy(() -> strategy.upload(file, "bucket1", "a.txt"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("上传文件不能为空");
        assertThatThrownBy(() -> strategy.upload(null, "bucket1", "a.txt"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void uploadShouldWrapIoFailure() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("a.txt");
        when(file.getSize()).thenReturn(3L);
        when(file.getInputStream()).thenThrow(new java.io.IOException("io"));

        assertThatThrownBy(() -> strategy.upload(file, "bucket1", null))
                .hasMessageContaining("文件上传失败");
    }

    @Test
    void getObjectShouldReturnStream() {
        OSSObject ossObject = mock(OSSObject.class);
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[]{1});
        when(ossObject.getObjectContent()).thenReturn(in);
        when(client.getObject("bucket1", "a.txt")).thenReturn(ossObject);

        assertThat(strategy.getObject("bucket1", "a.txt")).isSameAs(in);
    }

    @Test
    void getObjectShouldWrapFailure() {
        when(client.getObject("bucket1", "a.txt")).thenThrow(new RuntimeException("boom"));

        assertThatThrownBy(() -> strategy.getObject("bucket1", "a.txt"))
                .hasMessageContaining("获取文件失败");
    }

    @Test
    void getPreviewUrlShouldUseDefaultExpiry() throws Exception {
        when(client.generatePresignedUrl(eq("bucket1"), eq("a.txt"), any(Date.class)))
                .thenReturn(new URL("https://example.com/a.txt"));

        assertThat(strategy.getPreviewUrl("bucket1", "a.txt", 0))
                .isEqualTo("https://example.com/a.txt");

        when(client.generatePresignedUrl(eq("bucket1"), eq("a.txt"), any(Date.class)))
                .thenThrow(new RuntimeException("boom"));
        assertThatThrownBy(() -> strategy.getPreviewUrl("bucket1", "a.txt", 60))
                .hasMessageContaining("生成预览地址失败");
    }

    @Test
    void removeObjectShouldDelete() {
        strategy.removeObject("bucket1", "a.txt");

        verify(client).deleteObject("bucket1", "a.txt");
        verify(client).shutdown();
    }

    @Test
    void removeObjectShouldWrapFailure() {
        doThrow(new OSSException("x")).when(client).deleteObject("bucket1", "a.txt");

        assertThatThrownBy(() -> strategy.removeObject("bucket1", "a.txt"))
                .hasMessageContaining("删除对象失败");
    }

    @Test
    void removeObjectsShouldHandleEmptyAndNormal() {
        strategy.removeObjects("bucket1", null);
        strategy.removeObjects("bucket1", List.of());
        verify(client, never()).deleteObjects(any(DeleteObjectsRequest.class));

        strategy.removeObjects("bucket1", List.of("a.txt", "b.txt"));
        verify(client).deleteObjects(any(DeleteObjectsRequest.class));
    }

    @Test
    void removeObjectsShouldWrapFailure() {
        doThrow(new OSSException("x")).when(client).deleteObjects(any(DeleteObjectsRequest.class));

        assertThatThrownBy(() -> strategy.removeObjects("bucket1", List.of("a.txt")))
                .hasMessageContaining("批量删除对象失败");
    }

    @Test
    void removeObjectsShouldRejectInvalidObjectName() {
        assertThatThrownBy(() -> strategy.removeObjects("bucket1", List.of("../a")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getObjectShouldValidateNames() {
        assertThatThrownBy(() -> strategy.getObject("Bad", "a")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> strategy.getObject("bucket1", "../a")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void uploadShouldUseDefaultBucketAndContentType() throws Exception {
        properties.setBucketName(null);
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("a.txt");
        when(file.getContentType()).thenReturn("text/plain");
        when(file.getSize()).thenReturn(1L);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[]{1}));
        when(client.doesBucketExist("bucket1")).thenReturn(true);
        OSSObject ossObject = mock(OSSObject.class);
        when(ossObject.getObjectMetadata()).thenReturn(mock(ObjectMetadata.class));
        when(client.getObject("bucket1", "a.txt")).thenReturn(ossObject);
        when(client.generatePresignedUrl(eq("bucket1"), eq("a.txt"), any(Date.class)))
                .thenReturn(new URL("https://example.com/a.txt"));

        UploadResultVO result = strategy.upload(file, "bucket1", "a.txt");

        assertThat(result.getBucket()).isEqualTo("bucket1");
        verify(client, never()).createBucket(anyString());
    }
}
