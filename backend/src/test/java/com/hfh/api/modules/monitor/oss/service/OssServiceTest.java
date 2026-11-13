package com.hfh.api.modules.monitor.oss.service;

import com.hfh.api.modules.monitor.oss.config.OssProperties;
import com.hfh.api.modules.monitor.oss.enums.OssProvider;
import com.hfh.api.modules.monitor.oss.vo.BucketVO;
import com.hfh.api.modules.monitor.oss.vo.ObjectVO;
import com.hfh.api.modules.monitor.oss.vo.UploadResultVO;
import com.hfh.api.modules.system.service.ConfigService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OssServiceTest {

    @Mock
    private ConfigService configService;
    @Mock
    private OssStrategy strategy;

    private OssProperties properties;
    private OssService ossService;

    @BeforeEach
    void setUp() {
        properties = new OssProperties();
        properties.setProvider("minio");
        when(strategy.getProvider()).thenReturn(OssProvider.MINIO);
        ossService = new OssService(properties, configService, List.of(strategy));
    }

    @Test
    void getProviderShouldPreferSystemConfig() {
        when(configService.getConfigValue("oss.provider")).thenReturn("aliyun");

        assertThat(ossService.getProvider()).isEqualTo(OssProvider.ALIYUN);
    }

    @Test
    void getProviderShouldFallbackToYamlAndMinioForUnknown() {
        when(configService.getConfigValue("oss.provider")).thenReturn("");
        assertThat(ossService.getProvider()).isEqualTo(OssProvider.MINIO);

        when(configService.getConfigValue("oss.provider")).thenReturn("unknown");
        assertThat(ossService.getProvider()).isEqualTo(OssProvider.MINIO);
    }

    @Test
    void allOperationsShouldDelegateToCurrentStrategy() {
        when(strategy.listBuckets()).thenReturn(List.of(new BucketVO()));
        when(strategy.listObjects("b", "p", true)).thenReturn(List.of(new ObjectVO()));
        InputStream in = new java.io.ByteArrayInputStream(new byte[0]);
        when(strategy.getObject("b", "o")).thenReturn(in);
        when(strategy.getPreviewUrl("b", "o", 60)).thenReturn("url");
        MultipartFile file = mock(MultipartFile.class);
        UploadResultVO uploadResult = new UploadResultVO();
        when(strategy.upload(file, "b", "o")).thenReturn(uploadResult);

        assertThat(ossService.listBuckets()).hasSize(1);
        ossService.createBucket("b");
        ossService.removeBucket("b");
        assertThat(ossService.listObjects("b", "p", true)).hasSize(1);
        assertThat(ossService.upload(file, "b", "o")).isSameAs(uploadResult);
        assertThat(ossService.getObject("b", "o")).isSameAs(in);
        assertThat(ossService.getPreviewUrl("b", "o", 60)).isEqualTo("url");
        ossService.removeObject("b", "o");
        ossService.removeObjects("b", List.of("o"));

        verify(strategy).createBucket("b");
        verify(strategy).removeBucket("b");
        verify(strategy).removeObject("b", "o");
        verify(strategy).removeObjects("b", List.of("o"));
    }

    @Test
    void shouldRejectUnsupportedProvider() {
        when(configService.getConfigValue("oss.provider")).thenReturn("aliyun");

        assertThatThrownBy(() -> ossService.listBuckets())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("不支持的 OSS 提供商");
    }
}
