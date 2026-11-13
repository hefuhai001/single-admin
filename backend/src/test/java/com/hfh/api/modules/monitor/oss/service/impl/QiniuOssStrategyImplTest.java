package com.hfh.api.modules.monitor.oss.service.impl;

import com.hfh.api.modules.monitor.oss.config.OssProperties;
import com.hfh.api.modules.monitor.oss.enums.OssProvider;
import com.hfh.api.modules.monitor.oss.vo.BucketVO;
import com.hfh.api.modules.monitor.oss.vo.ObjectVO;
import com.hfh.api.modules.monitor.oss.vo.UploadResultVO;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.storage.model.FileInfo;
import com.qiniu.util.Auth;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class QiniuOssStrategyImplTest {

    private OssProperties properties;
    private Auth auth;
    private MockedStatic<Auth> authStatic;
    private QiniuOssStrategyImpl strategy;

    @BeforeEach
    void setUp() {
        properties = new OssProperties();
        properties.setAccessKey("ak");
        properties.setSecretKey("sk");
        properties.setBucketName("default-bucket");
        properties.setDomain("http://cdn.example.com");
        auth = mock(Auth.class);
        authStatic = mockStatic(Auth.class);
        authStatic.when(() -> Auth.create(any(), any())).thenReturn(auth);
        strategy = new QiniuOssStrategyImpl(properties);
    }

    @AfterEach
    void tearDown() {
        authStatic.close();
    }

    @Test
    void providerShouldBeQiniu() {
        assertThat(strategy.getProvider()).isEqualTo(OssProvider.QINIU);
    }

    @Test
    void listBucketsShouldMapNames() {
        try (MockedConstruction<BucketManager> mc = mockConstruction(BucketManager.class,
                (manager, ctx) -> when(manager.buckets()).thenReturn(new String[]{"b1", "b2"}))) {

            List<BucketVO> list = strategy.listBuckets();

            assertThat(list).extracting(BucketVO::getName).containsExactly("b1", "b2");
        }
    }

    @Test
    void listBucketsShouldWrapFailure() {
        try (MockedConstruction<BucketManager> mc = mockConstruction(BucketManager.class,
                (manager, ctx) -> when(manager.buckets()).thenThrow(mock(QiniuException.class)))) {

            assertThatThrownBy(() -> strategy.listBuckets()).hasMessageContaining("查询存储桶失败");
        }
    }

    @Test
    void createBucketShouldRejectExistingAndCreateMissing() throws Exception {
        try (MockedConstruction<BucketManager> mc = mockConstruction(BucketManager.class,
                (manager, ctx) -> when(manager.buckets()).thenReturn(new String[]{"bucket1"}))) {

            assertThatThrownBy(() -> strategy.createBucket("bucket1")).hasMessage("存储桶已存在");
        }

        try (MockedConstruction<BucketManager> mc = mockConstruction(BucketManager.class,
                (manager, ctx) -> when(manager.buckets()).thenReturn(new String[0]))) {
            properties.setRegion("z1");

            strategy.createBucket("bucket1");

            BucketManager manager = mc.constructed().get(0);
            verify(manager).createBucket("bucket1", "z1");
        }
    }

    @Test
    void createBucketShouldUseZ0WhenRegionMissing() throws Exception {
        properties.setRegion("");
        try (MockedConstruction<BucketManager> mc = mockConstruction(BucketManager.class,
                (manager, ctx) -> when(manager.buckets()).thenReturn(new String[0]))) {

            strategy.createBucket("bucket1");

            verify(mc.constructed().get(0)).createBucket("bucket1", "z0");
        }
    }

    @Test
    void createBucketShouldCoverAllRegionMappings() throws Exception {
        for (String region : List.of("z0", "huadong", "z1", "huabei", "z2", "huanan",
                "na0", "beimei", "as0", "xinjiapo", "cn-east-2", "huadongZheJiang2", "unknown")) {
            properties.setRegion(region);
            try (MockedConstruction<BucketManager> mc = mockConstruction(BucketManager.class,
                    (manager, ctx) -> when(manager.buckets()).thenReturn(new String[0]))) {
                strategy.createBucket("bucket1");
                assertThat(mc.constructed()).hasSize(1);
            }
        }
    }

    @Test
    void createBucketShouldWrapCheckedFailure() {
        try (MockedConstruction<BucketManager> mc = mockConstruction(BucketManager.class,
                (manager, ctx) -> when(manager.buckets()).thenThrow(mock(QiniuException.class)))) {

            assertThatThrownBy(() -> strategy.createBucket("bucket1"))
                    .hasMessageContaining("创建存储桶失败");
        }
    }

    @Test
    void removeBucketShouldDelete() throws Exception {
        try (MockedConstruction<BucketManager> mc = mockConstruction(BucketManager.class)) {

            strategy.removeBucket("bucket1");

            verify(mc.constructed().get(0)).delete("bucket1", null);
        }
    }

    @Test
    void removeBucketShouldWrapFailure() {
        try (MockedConstruction<BucketManager> mc = mockConstruction(BucketManager.class,
                (manager, ctx) -> doThrow(mock(QiniuException.class)).when(manager).delete(anyString(), any()))) {

            assertThatThrownBy(() -> strategy.removeBucket("bucket1"))
                    .hasMessageContaining("删除存储桶失败");
        }
    }

    @Test
    void listObjectsShouldMapFileInfo() {
        FileInfo info = new FileInfo();
        info.key = "a.txt";
        info.fsize = 10;
        info.putTime = 10000;
        info.hash = "h1";
        try (MockedConstruction<BucketManager> mc = mockConstruction(BucketManager.class,
                (manager, ctx) -> {
                    BucketManager.FileListIterator iterator = mock(BucketManager.FileListIterator.class);
                    when(iterator.hasNext()).thenReturn(true, false);
                    when(iterator.next()).thenReturn(new FileInfo[]{info});
                    when(manager.createFileListIterator(anyString(), any(), anyInt(), any()))
                            .thenReturn(iterator);
                })) {

            List<ObjectVO> list = strategy.listObjects("bucket1", "p", true);

            assertThat(list).hasSize(1);
            assertThat(list.get(0).getObjectName()).isEqualTo("a.txt");
            assertThat(list.get(0).getSize()).isEqualTo(10L);
            assertThat(list.get(0).getEtag()).isEqualTo("h1");
        }
    }

    @Test
    void listObjectsShouldWrapFailure() {
        try (MockedConstruction<BucketManager> mc = mockConstruction(BucketManager.class,
                (manager, ctx) -> when(manager.createFileListIterator(anyString(), any(), anyInt(), any()))
                        .thenThrow(mock(QiniuException.class)))) {

            assertThatThrownBy(() -> strategy.listObjects("bucket1", null, false))
                    .hasMessageContaining("查询对象列表失败");
        }
    }

    @Test
    void uploadShouldReturnResult() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("a.txt");
        when(file.getSize()).thenReturn(3L);
        when(file.getInputStream()).thenReturn(new java.io.ByteArrayInputStream(new byte[]{1, 2, 3}));
        when(auth.uploadToken("bucket1")).thenReturn("token");
        when(auth.privateDownloadUrl(anyString(), anyLong())).thenReturn("http://preview/a.txt");
        Response response = mock(Response.class);
        when(response.isOK()).thenReturn(true);
        DefaultPutRet putRet = new DefaultPutRet();
        putRet.hash = "hash-1";
        when(response.jsonToObject(DefaultPutRet.class)).thenReturn(putRet);
        try (MockedConstruction<UploadManager> mc = mockConstruction(UploadManager.class,
                (manager, ctx) -> when(manager.put(any(byte[].class), anyString(), anyString()))
                        .thenReturn(response))) {

            UploadResultVO result = strategy.upload(file, "bucket1", null);

            assertThat(result.getBucket()).isEqualTo("bucket1");
            assertThat(result.getObjectName()).isEqualTo("a.txt");
            assertThat(result.getEtag()).isEqualTo("hash-1");
            assertThat(result.getUrl()).isEqualTo("http://preview/a.txt");
            assertThat(result.getPermanentUrl()).isEqualTo("http://cdn.example.com/a.txt");
        }
    }

    @Test
    void uploadShouldFailWhenResponseNotOk() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("a.txt");
        when(file.getSize()).thenReturn(1L);
        when(file.getInputStream()).thenReturn(new java.io.ByteArrayInputStream(new byte[]{1}));
        when(auth.uploadToken("bucket1")).thenReturn("token");
        Response response = mock(Response.class);
        when(response.isOK()).thenReturn(false);
        try (MockedConstruction<UploadManager> mc = mockConstruction(UploadManager.class,
                (manager, ctx) -> when(manager.put(any(byte[].class), anyString(), anyString()))
                        .thenReturn(response))) {

            assertThatThrownBy(() -> strategy.upload(file, "bucket1", null))
                    .hasMessageContaining("上传失败");
        }
    }

    @Test
    void uploadShouldRejectEmptyFileAndWrapIoError() throws Exception {
        MultipartFile empty = mock(MultipartFile.class);
        when(empty.isEmpty()).thenReturn(true);
        assertThatThrownBy(() -> strategy.upload(empty, "bucket1", "a.txt"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> strategy.upload(null, "bucket1", "a.txt"))
                .isInstanceOf(IllegalArgumentException.class);

        MultipartFile broken = mock(MultipartFile.class);
        when(broken.isEmpty()).thenReturn(false);
        when(broken.getOriginalFilename()).thenReturn("a.txt");
        when(broken.getInputStream()).thenThrow(new java.io.IOException("io"));
        assertThatThrownBy(() -> strategy.upload(broken, "bucket1", null))
                .hasMessageContaining("文件上传失败");
    }

    @Test
    void getObjectShouldReadFromUrl(@TempDir Path tempDir) throws Exception {
        Path file = Files.writeString(tempDir.resolve("a.txt"), "hello");
        when(auth.privateDownloadUrl(anyString(), anyLong())).thenReturn(file.toUri().toString());

        try (InputStream in = strategy.getObject("bucket1", "a.txt")) {
            assertThat(new String(in.readAllBytes())).isEqualTo("hello");
        }
    }

    @Test
    void getObjectShouldWrapFailure() {
        when(auth.privateDownloadUrl(anyString(), anyLong())).thenThrow(new RuntimeException("sign"));

        assertThatThrownBy(() -> strategy.getObject("bucket1", "a.txt"))
                .hasMessageContaining("获取文件失败");
    }

    @Test
    void getPreviewUrlShouldUseDomainAndExpiry() {
        when(auth.privateDownloadUrl(anyString(), anyLong())).thenReturn("signed");

        assertThat(strategy.getPreviewUrl("bucket1", "a.txt", 0)).isEqualTo("signed");

        properties.setCustomDomain("http://custom.example.com");
        assertThat(strategy.getPreviewUrl("bucket1", "a.txt", 60)).isEqualTo("signed");
    }

    @Test
    void urlShouldFailWhenDomainMissing() {
        properties.setDomain("");
        properties.setCustomDomain("");

        assertThatThrownBy(() -> strategy.getPreviewUrl("bucket1", "a.txt", 10))
                .hasMessageContaining("七牛云域名未配置");
    }

    @Test
    void buildPermanentUrlShouldAppendSlash() {
        assertThat(strategy.buildPermanentUrl("bucket1", "a.txt"))
                .isEqualTo("http://cdn.example.com/a.txt");

        properties.setDomain("http://cdn.example.com/");
        assertThat(strategy.buildPermanentUrl("bucket1", "a.txt"))
                .isEqualTo("http://cdn.example.com/a.txt");
    }

    @Test
    void removeObjectShouldDeleteAndWrapQiniuException() throws Exception {
        try (MockedConstruction<BucketManager> mc = mockConstruction(BucketManager.class)) {
            strategy.removeObject("bucket1", "a.txt");
            verify(mc.constructed().get(0)).delete("bucket1", "a.txt");
        }

        try (MockedConstruction<BucketManager> mc = mockConstruction(BucketManager.class,
                (manager, ctx) -> doThrow(mock(QiniuException.class)).when(manager)
                        .delete(anyString(), anyString()))) {
            assertThatThrownBy(() -> strategy.removeObject("bucket1", "a.txt"))
                    .hasMessageContaining("删除对象失败");
        }
    }

    @Test
    void removeObjectsShouldDeleteEachAndHandleEmpty() throws Exception {
        try (MockedConstruction<BucketManager> mc = mockConstruction(BucketManager.class)) {
            strategy.removeObjects("bucket1", null);
            strategy.removeObjects("bucket1", List.of());
            strategy.removeObjects("bucket1", List.of("a.txt", "b.txt"));
            verify(mc.constructed().get(0), org.mockito.Mockito.times(2)).delete(eq("bucket1"), anyString());
        }
    }

    @Test
    void removeObjectsShouldWrapFailure() {
        try (MockedConstruction<BucketManager> mc = mockConstruction(BucketManager.class,
                (manager, ctx) -> doThrow(mock(QiniuException.class)).when(manager)
                        .delete(anyString(), anyString()))) {

            assertThatThrownBy(() -> strategy.removeObjects("bucket1", List.of("a.txt")))
                    .hasMessageContaining("批量删除对象失败");
        }
    }
}
