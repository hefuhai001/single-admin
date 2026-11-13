package com.hfh.api.modules.monitor.oss.service.impl;

import com.hfh.api.modules.monitor.oss.config.OssProperties;
import com.hfh.api.modules.monitor.oss.enums.OssProvider;
import com.hfh.api.modules.monitor.oss.vo.BucketVO;
import com.hfh.api.modules.monitor.oss.vo.ObjectVO;
import com.hfh.api.modules.monitor.oss.vo.UploadResultVO;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.StatObjectResponse;
import io.minio.messages.Bucket;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MinioOssStrategyImplTest {

    @Mock
    private ObjectProvider<MinioClient> provider;
    @Mock
    private MinioClient client;

    private OssProperties properties;
    private MinioOssStrategyImpl strategy;

    @BeforeEach
    void setUp() {
        properties = new OssProperties();
        properties.setBucketName("default-bucket");
        properties.setCustomDomain("http://cdn.example.com");
        strategy = new MinioOssStrategyImpl(properties, provider);
        when(provider.getIfAvailable(any())).thenReturn(client);
    }

    @Test
    void providerShouldBeMinio() {
        assertThat(strategy.getProvider()).isEqualTo(OssProvider.MINIO);
    }

    @Test
    void listBucketsShouldMap() throws Exception {
        Bucket bucket = mock(Bucket.class);
        when(bucket.name()).thenReturn("b1");
        when(bucket.creationDate()).thenReturn(ZonedDateTime.now(ZoneOffset.UTC));
        when(client.listBuckets()).thenReturn(List.of(bucket));

        List<BucketVO> list = strategy.listBuckets();

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getName()).isEqualTo("b1");
    }

    @Test
    void listBucketsShouldWrapFailure() throws Exception {
        when(client.listBuckets()).thenThrow(new RuntimeException("minio down"));

        assertThatThrownBy(() -> strategy.listBuckets()).hasMessageContaining("查询存储桶失败");
    }

    @Test
    void missingClientShouldProduceError() {
        when(provider.getIfAvailable(any())).thenThrow(new RuntimeException("client missing"));

        assertThatThrownBy(() -> strategy.listBuckets()).hasMessageContaining("查询存储桶失败");
    }

    @Test
    void createBucketShouldRejectExisting() throws Exception {
        when(client.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);

        assertThatThrownBy(() -> strategy.createBucket("bucket1")).hasMessage("存储桶已存在");
        verify(client, never()).makeBucket(any(MakeBucketArgs.class));
    }

    @Test
    void createBucketShouldCreateWhenMissing() throws Exception {
        when(client.bucketExists(any(BucketExistsArgs.class))).thenReturn(false);

        strategy.createBucket("bucket1");

        verify(client).makeBucket(any(MakeBucketArgs.class));
    }

    @Test
    void createBucketShouldWrapCheckedFailure() throws Exception {
        when(client.bucketExists(any(BucketExistsArgs.class))).thenThrow(new java.io.IOException("checked"));

        assertThatThrownBy(() -> strategy.createBucket("bucket1")).hasMessageContaining("创建存储桶失败");
    }

    @Test
    void removeBucketShouldHandleFailure() throws Exception {
        strategy.removeBucket("bucket1");
        verify(client).removeBucket(any());

        doThrow(new java.io.IOException("x")).when(client).removeBucket(any());
        assertThatThrownBy(() -> strategy.removeBucket("bucket1"))
                .hasMessageContaining("删除存储桶失败");
    }

    @Test
    void listObjectsShouldMapItems() throws Exception {
        Item item = mock(Item.class);
        when(item.objectName()).thenReturn("a.txt");
        when(item.size()).thenReturn(10L);
        when(item.lastModified()).thenReturn(ZonedDateTime.now(ZoneOffset.UTC));
        when(item.etag()).thenReturn("etag");
        @SuppressWarnings("unchecked")
        io.minio.Result<Item> result = mock(io.minio.Result.class);
        when(result.get()).thenReturn(item);
        when(client.listObjects(any(io.minio.ListObjectsArgs.class))).thenReturn(List.of(result));

        List<ObjectVO> list = strategy.listObjects("bucket1", "p", true);

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getObjectName()).isEqualTo("a.txt");
        assertThat(list.get(0).getEtag()).isEqualTo("etag");
    }

    @Test
    void listObjectsShouldWrapFailure() throws Exception {
        when(client.listObjects(any(io.minio.ListObjectsArgs.class))).thenThrow(new RuntimeException("x"));

        assertThatThrownBy(() -> strategy.listObjects("bucket1", null, false))
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
        when(client.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);
        StatObjectResponse stat = mock(StatObjectResponse.class);
        when(stat.etag()).thenReturn("etag-1");
        when(client.statObject(any())).thenReturn(stat);
        when(client.getPresignedObjectUrl(any())).thenReturn("http://minio/a.txt");

        UploadResultVO result = strategy.upload(file, "bucket1", null);

        assertThat(result.getBucket()).isEqualTo("bucket1");
        assertThat(result.getObjectName()).isEqualTo("a.txt");
        assertThat(result.getEtag()).isEqualTo("etag-1");
        assertThat(result.getUrl()).isEqualTo("http://minio/a.txt");
        assertThat(result.getPermanentUrl()).isEqualTo("http://cdn.example.com/bucket1/a.txt");
    }

    @Test
    void uploadShouldCreateBucketAndHandleFailure() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("a.txt");
        when(file.getSize()).thenReturn(1L);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[]{1}));
        when(client.bucketExists(any(BucketExistsArgs.class))).thenReturn(false);
        doThrow(new java.io.IOException("put fail")).when(client).putObject(any());

        assertThatThrownBy(() -> strategy.upload(file, "bucket1", null))
                .hasMessageContaining("文件上传失败");
        verify(client).makeBucket(any(MakeBucketArgs.class));
    }

    @Test
    void uploadShouldRejectEmptyFile() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        assertThatThrownBy(() -> strategy.upload(file, "bucket1", "a.txt"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> strategy.upload(null, "bucket1", "a.txt"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getObjectShouldReturnStreamAndWrapFailure() throws Exception {
        io.minio.GetObjectResponse response = mock(io.minio.GetObjectResponse.class);
        when(client.getObject(any(GetObjectArgs.class))).thenReturn(response);

        assertThat(strategy.getObject("bucket1", "a.txt")).isSameAs(response);

        when(client.getObject(any(GetObjectArgs.class))).thenThrow(new java.io.IOException("x"));
        assertThatThrownBy(() -> strategy.getObject("bucket1", "a.txt"))
                .hasMessageContaining("获取文件失败");
    }

    @Test
    void getPreviewUrlShouldDefaultExpiryAndWrapFailure() throws Exception {
        when(client.getPresignedObjectUrl(any())).thenReturn("http://minio/a.txt");

        assertThat(strategy.getPreviewUrl("bucket1", "a.txt", -1)).isEqualTo("http://minio/a.txt");

        when(client.getPresignedObjectUrl(any())).thenThrow(new java.io.IOException("x"));
        assertThatThrownBy(() -> strategy.getPreviewUrl("bucket1", "a.txt", 60))
                .hasMessageContaining("生成预览地址失败");
    }

    @Test
    void removeObjectShouldHandleFailure() throws Exception {
        strategy.removeObject("bucket1", "a.txt");
        verify(client).removeObject(any());

        doThrow(new java.io.IOException("x")).when(client).removeObject(any());
        assertThatThrownBy(() -> strategy.removeObject("bucket1", "a.txt"))
                .hasMessageContaining("删除对象失败");
    }

    @Test
    void removeObjectsShouldHandleEmptyAndErrors() throws Exception {
        strategy.removeObjects("bucket1", null);
        strategy.removeObjects("bucket1", List.of());
        verify(client, never()).removeObjects(any());

        when(client.removeObjects(any())).thenReturn(List.of());
        strategy.removeObjects("bucket1", List.of("a.txt"));
        verify(client).removeObjects(any());

        @SuppressWarnings("unchecked")
        io.minio.Result<DeleteError> result = mock(io.minio.Result.class);
        DeleteError error = mock(DeleteError.class);
        when(error.objectName()).thenReturn("a.txt");
        when(error.message()).thenReturn("denied");
        when(result.get()).thenReturn(error);
        when(client.removeObjects(any())).thenReturn(List.of(result));

        assertThatThrownBy(() -> strategy.removeObjects("bucket1", List.of("a.txt")))
                .hasMessageContaining("部分对象删除失败");
    }

    @Test
    void removeObjectsShouldWrapCheckedFailure() throws Exception {
        when(client.removeObjects(any())).thenAnswer(inv -> {
            throw new java.io.IOException("x");
        });

        assertThatThrownBy(() -> strategy.removeObjects("bucket1", List.of("a.txt")))
                .hasMessageContaining("批量删除对象失败");
    }

    @Test
    void removeObjectsShouldValidateNames() {
        assertThatThrownBy(() -> strategy.removeObjects("bucket1", List.of("a\\b")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void bucketValidationShouldRejectBadNames() {
        assertThatThrownBy(() -> strategy.listObjects("Bad", null, false))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> strategy.getObject("bucket1", "../a"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> strategy.getPreviewUrl("bucket1", "/a", 10))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> strategy.removeObject("bucket1", ""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deleteObjectsShouldIgnoreIndividualErrors() throws Exception {
        @SuppressWarnings("unchecked")
        io.minio.Result<DeleteError> result = mock(io.minio.Result.class);
        when(result.get()).thenThrow(new RuntimeException("no such key"));
        when(client.removeObjects(any())).thenReturn(List.of(result));

        strategy.removeObjects("bucket1", List.of("a.txt"));
    }
}
