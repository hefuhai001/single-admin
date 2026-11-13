package com.hfh.api.modules.monitor.oss.service.support;

import com.hfh.api.modules.monitor.oss.config.OssProperties;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AbstractOssStrategyTest {

    static class TestStrategy extends AbstractOssStrategy {
        TestStrategy(OssProperties properties) {
            super(properties);
        }

        String bucket(String bucketName) {
            return defaultBucketName(bucketName);
        }

        String object(MultipartFile file, String objectName) {
            return defaultObjectName(file, objectName);
        }
    }

    private final OssProperties properties = new OssProperties();
    private final TestStrategy strategy = new TestStrategy(properties);

    @Test
    void defaultBucketNameShouldFallbackToProperties() {
        properties.setBucketName("default-bucket");

        assertThat(strategy.bucket("given")).isEqualTo("given");
        assertThat(strategy.bucket("  ")).isEqualTo("default-bucket");
        assertThat(strategy.bucket(null)).isEqualTo("default-bucket");
    }

    @Test
    void defaultObjectNameShouldUseFileNameWhenMissing() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("a.txt");

        assertThat(strategy.object(file, "custom")).isEqualTo("custom");
        assertThat(strategy.object(file, "")).isEqualTo("a.txt");
    }

    @Test
    void defaultObjectNameShouldRejectBlank() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("");

        assertThatThrownBy(() -> strategy.object(file, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("对象名称不能为空");
    }

    @Test
    void bucketValidationShouldFollowRules() {
        strategy.validateBucketName("abc");
        strategy.validateBucketName("a1-b2");

        assertThatThrownBy(() -> strategy.validateBucketName(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> strategy.validateBucketName("")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> strategy.validateBucketName("Abc")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> strategy.validateBucketName("-abc")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> strategy.validateBucketName("ab")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> strategy.validateBucketName("a".repeat(64)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void objectValidationShouldRejectUnsafeNames() {
        strategy.validateObjectName("folder/a.txt");

        assertThatThrownBy(() -> strategy.validateObjectName(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> strategy.validateObjectName(" ")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> strategy.validateObjectName("../a")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> strategy.validateObjectName("a\\b")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> strategy.validateObjectName("/a")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void buildPermanentUrlShouldPreferCustomDomain() {
        properties.setEndpoint("http://endpoint");
        properties.setCustomDomain("http://cdn");

        assertThat(strategy.buildPermanentUrl("b", "o")).isEqualTo("http://cdn/b/o");

        properties.setCustomDomain("http://cdn/");
        assertThat(strategy.buildPermanentUrl("b", "o")).isEqualTo("http://cdn/b/o");

        properties.setCustomDomain(null);
        assertThat(strategy.buildPermanentUrl("b", "o")).isEqualTo("http://endpoint/b/o");

        properties.setEndpoint("");
        assertThat(strategy.buildPermanentUrl("b", "o")).isEmpty();
    }
}
