package com.hfh.api.modules.monitor.oss.config;

import com.hfh.api.modules.system.service.ConfigService;
import io.minio.MinioClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DynamicOssConfigTest {

    @Mock
    private ConfigService configService;

    private DynamicOssConfig config;

    @BeforeEach
    void setUp() {
        config = new DynamicOssConfig();
        ReflectionTestUtils.setField(config, "configService", configService);
    }

    @Test
    void yamlPropertiesShouldBeEmptyDefaults() {
        OssProperties properties = config.yamlOssProperties();

        assertThat(properties.getProvider()).isEqualTo("minio");
    }

    @Test
    void ossPropertiesShouldMergeSystemConfig() {
        when(configService.getConfigValue(anyString())).thenReturn(null);
        when(configService.getConfigValue("oss.provider")).thenReturn("aliyun");
        when(configService.getConfigValue("oss.endpoint")).thenReturn("http://endpoint");
        when(configService.getConfigValue("oss.accessKey")).thenReturn("ak");
        when(configService.getConfigValue("oss.secretKey")).thenReturn("sk");
        when(configService.getConfigValue("oss.bucket_name")).thenReturn("bucket");
        when(configService.getConfigValue("oss.custom_domain")).thenReturn("http://cdn");
        when(configService.getConfigValue("oss.region")).thenReturn("cn-hangzhou");
        when(configService.getConfigValue("oss.domain")).thenReturn("http://qiniu");

        OssProperties properties = config.ossProperties();

        assertThat(properties.getProvider()).isEqualTo("aliyun");
        assertThat(properties.getEndpoint()).isEqualTo("http://endpoint");
        assertThat(properties.getAccessKey()).isEqualTo("ak");
        assertThat(properties.getSecretKey()).isEqualTo("sk");
        assertThat(properties.getBucketName()).isEqualTo("bucket");
        assertThat(properties.getCustomDomain()).isEqualTo("http://cdn");
        assertThat(properties.getRegion()).isEqualTo("cn-hangzhou");
        assertThat(properties.getDomain()).isEqualTo("http://qiniu");
    }

    @Test
    void minioClientShouldBuildWhenConfigured() {
        when(configService.getConfigValue("oss.endpoint")).thenReturn("http://localhost:9000");
        when(configService.getConfigValue("oss.accessKey")).thenReturn("ak");
        when(configService.getConfigValue("oss.secretKey")).thenReturn("sk");

        MinioClient client = config.minioClient();

        assertThat(client).isNotNull();
    }

    @Test
    void minioClientShouldRejectIncompleteConfig() {
        when(configService.getConfigValue("oss.endpoint")).thenReturn("");

        assertThatThrownBy(() -> config.minioClient())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("MinIO 配置不完整");
    }
}
