package com.hfh.api.modules.monitor.oss.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.util.SaResult;
import com.hfh.api.base.BaseController;
import com.hfh.api.modules.monitor.oss.config.MinioProperties;
import com.hfh.api.modules.monitor.oss.service.MinioService;
import com.hfh.api.modules.monitor.oss.vo.BucketVO;
import com.hfh.api.modules.monitor.oss.vo.ObjectVO;
import com.hfh.api.modules.monitor.oss.vo.UploadResultVO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MinIO 文件管理接口
 */
@RestController(value = "minioController")
@RequestMapping("/admin/oss")
@RequiredArgsConstructor
@Tag(name = "MinIO文件管理", description = "MinIO 存储桶与文件管理接口")
public class MinioController extends BaseController {

    private final MinioService minioService;
    private final MinioProperties minioProperties;

    // ==================== 存储桶管理 ====================

    @SaCheckPermission("oss:bucket:query")
    @GetMapping("/buckets")
    @Operation(summary = "查询存储桶列表")
    public SaResult listBuckets() {
        List<BucketVO> buckets = minioService.listBuckets();
        return SaResult.data(buckets);
    }

    @SaCheckPermission("oss:bucket:add")
    @PostMapping("/buckets/{bucketName}")
    @Operation(summary = "创建存储桶")
    public SaResult createBucket(
            @Parameter(description = "存储桶名称") @PathVariable("bucketName") String bucketName) {
        minioService.createBucket(bucketName);
        return SaResult.ok("创建成功");
    }

    @SaCheckPermission("oss:bucket:delete")
    @DeleteMapping("/buckets/{bucketName}")
    @Operation(summary = "删除存储桶")
    public SaResult deleteBucket(
            @Parameter(description = "存储桶名称") @PathVariable("bucketName") String bucketName) {
        minioService.removeBucket(bucketName);
        return SaResult.ok("删除成功");
    }

    // ==================== 对象/文件管理 ====================

    @SaCheckPermission("oss:object:query")
    @GetMapping("/objects")
    @Operation(summary = "分页查询对象列表")
    public SaResult listObjects(
            @Parameter(description = "存储桶名称") @RequestParam String bucket,
            @Parameter(description = "前缀过滤") @RequestParam(required = false) String prefix,
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "是否递归查询") @RequestParam(defaultValue = "true") Boolean recursive) {

        String bucketName = StringUtils.hasText(bucket) ? bucket : minioProperties.getBucketName();
        List<ObjectVO> all = minioService.listObjects(bucketName, prefix, Boolean.TRUE.equals(recursive));

        int total = all.size();
        int from = (current - 1) * size;
        int to = Math.min(from + size, total);
        List<ObjectVO> records = from < total ? all.subList(from, to) : List.of();

        Map<String, Object> page = new HashMap<>();
        page.put("records", records);
        page.put("total", total);
        page.put("current", current);
        page.put("size", size);
        return SaResult.data(page);
    }

    @SaCheckPermission("oss:object:upload")
    @PostMapping("/objects/upload")
    @Operation(summary = "上传文件")
    public SaResult upload(
            @Parameter(description = "文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "存储桶名称，为空则使用默认桶") @RequestParam(required = false) String bucket,
            @Parameter(description = "对象名称，为空则使用原文件名") @RequestParam(required = false) String objectName) {
        String bucketName = StringUtils.hasText(bucket) ? bucket : minioProperties.getBucketName();
        UploadResultVO result = minioService.upload(file, bucketName, objectName);
        return SaResult.data(result);
    }

    @SaCheckPermission("oss:object:download")
    @GetMapping("/objects/download")
    @Operation(summary = "下载文件")
    public ResponseEntity<InputStreamResource> download(
            @Parameter(description = "存储桶名称") @RequestParam String bucket,
            @Parameter(description = "对象名称") @RequestParam String objectName,
            HttpServletResponse response) {
        String bucketName = StringUtils.hasText(bucket) ? bucket : minioProperties.getBucketName();
        InputStream inputStream = minioService.getObject(bucketName, objectName);

        String filename = objectName.contains("/") ? objectName.substring(objectName.lastIndexOf("/") + 1) : objectName;
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"; filename*=UTF-8''" + encoded);
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        return ResponseEntity.ok()
                .headers(headers)
                .body(new InputStreamResource(inputStream));
    }

    @SaCheckPermission("oss:object:preview")
    @GetMapping("/objects/preview")
    @Operation(summary = "获取文件预览地址")
    public SaResult preview(
            @Parameter(description = "存储桶名称") @RequestParam String bucket,
            @Parameter(description = "对象名称") @RequestParam String objectName,
            @Parameter(description = "有效期（秒）") @RequestParam(defaultValue = "3600") Integer expiry) {
        String bucketName = StringUtils.hasText(bucket) ? bucket : minioProperties.getBucketName();
        String url = minioService.getPreviewUrl(bucketName, objectName, expiry);
        return SaResult.data(url);
    }

    @SaCheckPermission("oss:object:delete")
    @DeleteMapping("/objects")
    @Operation(summary = "删除对象")
    public SaResult deleteObject(
            @Parameter(description = "存储桶名称") @RequestParam String bucket,
            @Parameter(description = "对象名称") @RequestParam String objectName) {
        String bucketName = StringUtils.hasText(bucket) ? bucket : minioProperties.getBucketName();
        minioService.removeObject(bucketName, objectName);
        return SaResult.ok("删除成功");
    }

    @SaCheckPermission("oss:object:delete")
    @DeleteMapping("/objects/batch")
    @Operation(summary = "批量删除对象")
    public SaResult deleteObjectsBatch(
            @Parameter(description = "存储桶名称") @RequestParam String bucket,
            @Parameter(description = "对象名称列表") @RequestBody List<String> objectNames) {
        if (objectNames == null || objectNames.isEmpty()) {
            return SaResult.error("请选择要删除的文件");
        }
        String bucketName = StringUtils.hasText(bucket) ? bucket : minioProperties.getBucketName();
        minioService.removeObjects(bucketName, objectNames);
        return SaResult.ok("批量删除成功");
    }
}
