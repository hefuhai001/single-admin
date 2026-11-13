package com.hfh.api.modules.monitor.oss.controller;

import cn.dev33.satoken.util.SaResult;
import com.hfh.api.modules.monitor.oss.config.OssProperties;
import com.hfh.api.modules.monitor.oss.service.OssService;
import com.hfh.api.modules.monitor.oss.vo.BucketVO;
import com.hfh.api.modules.monitor.oss.vo.ObjectVO;
import com.hfh.api.modules.monitor.oss.vo.UploadResultVO;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OssControllerTest {

    private final OssService ossService = mock(OssService.class);
    private final OssProperties properties = new OssProperties();
    private final OssController controller = new OssController(ossService, properties);

    @Test
    void bucketEndpointsShouldDelegate() {
        when(ossService.listBuckets()).thenReturn(List.of(new BucketVO()));

        assertThat((List<?>) controller.listBuckets().getData()).hasSize(1);
        assertThat(controller.createBucket("b").getMsg()).isEqualTo("创建成功");
        assertThat(controller.deleteBucket("b").getMsg()).isEqualTo("删除成功");
    }

    @Test
    void listObjectsShouldPaginate() {
        ObjectVO first = new ObjectVO();
        ObjectVO second = new ObjectVO();
        when(ossService.listObjects("default", null, true)).thenReturn(List.of(first, second));
        when(ossService.listObjects("b", "p", false)).thenReturn(List.of(first, second));
        properties.setBucketName("default");

        SaResult result = controller.listObjects("", null, 3, 1, true);

        Map<?, ?> page = (Map<?, ?>) result.getData();
        assertThat(((Number) page.get("total")).intValue()).isEqualTo(2);
        assertThat((List<?>) page.get("records")).isEmpty();

        SaResult secondPage = controller.listObjects("b", "p", 1, 1, false);
        assertThat((List<?>) ((Map<?, ?>) secondPage.getData()).get("records")).hasSize(1);
        verify(ossService).listObjects("default", null, true);
        verify(ossService).listObjects("b", "p", false);
    }

    @Test
    void uploadAndPreviewShouldUseDefaultBucket() {
        MultipartFile file = mock(MultipartFile.class);
        UploadResultVO vo = new UploadResultVO();
        when(ossService.upload(file, "default", null)).thenReturn(vo);
        when(ossService.getPreviewUrl("b", "o", 3600)).thenReturn("url");
        properties.setBucketName("default");

        assertThat(controller.upload(file, "", null).getData()).isSameAs(vo);
        assertThat(controller.preview("b", "o", 3600).getData()).isEqualTo("url");
    }

    @Test
    void downloadShouldWriteStream() throws Exception {
        HttpServletResponse response = mock(HttpServletResponse.class);
        ServletOutputStream out = mock(ServletOutputStream.class);
        when(response.getOutputStream()).thenReturn(out);
        when(ossService.getObject("b", "dir/file.txt"))
                .thenReturn(new ByteArrayInputStream("data".getBytes()));

        controller.download("b", "dir/file.txt", response);

        verify(response).setContentType("application/octet-stream");
        verify(out).flush();
    }

    @Test
    void downloadShouldWrapFailure() {
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(ossService.getObject(anyString(), anyString()))
                .thenThrow(new RuntimeException("missing"));

        assertThatThrownBy(() -> controller.download("b", "a.txt", response))
                .hasMessageContaining("下载文件失败");
    }

    @Test
    void deleteEndpointsShouldValidateEmptyBatch() {
        assertThat(controller.deleteObject("b", "o").getMsg()).isEqualTo("删除成功");
        assertThat(controller.deleteObjectsBatch("b", null).getMsg()).isEqualTo("请选择要删除的文件");
        assertThat(controller.deleteObjectsBatch("b", List.of()).getMsg()).isEqualTo("请选择要删除的文件");
        assertThat(controller.deleteObjectsBatch("b", List.of("o")).getMsg()).isEqualTo("批量删除成功");

        properties.setBucketName("default");
        controller.deleteObject("", "o");
        verify(ossService).removeObject("default", "o");
        verify(ossService).removeObjects(org.mockito.ArgumentMatchers.eq("b"), anyList());
    }

    @Test
    void previewShouldUseDefaultExpiryContract() {
        when(ossService.getPreviewUrl(anyString(), anyString(), anyInt())).thenReturn("u");

        controller.preview("b", "o", 3600);

        verify(ossService).getPreviewUrl("b", "o", 3600);
        verify(ossService, never()).removeObject(any(), any());
    }
}
