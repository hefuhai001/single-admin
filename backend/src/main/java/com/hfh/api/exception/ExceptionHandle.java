package com.hfh.api.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.dev33.satoken.util.SaResult;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Hidden
@Slf4j
@RestControllerAdvice
public class ExceptionHandle {

    // 未登录
    @ExceptionHandler(NotLoginException.class)
    public SaResult handlerNotLogin(NotLoginException e) {
        // 区分 token 过期类型，前端据此判断是否可尝试刷新
        String message;
        int code;
        if (NotLoginException.TOKEN_TIMEOUT.equals(e.getType())) {
            // accessToken 过期，前端可用 refreshToken 刷新
            message = "accessToken 已过期";
            code = 401;
        } else if (NotLoginException.KICK_OUT.equals(e.getType())) {
            message = "已被踢下线";
            code = 401;
        } else {
            message = "请先登录";
            code = 401;
        }
        return SaResult.code(code).setMsg(message);
    }

    // 缺少权限
    @ExceptionHandler(NotPermissionException.class)
    public SaResult handlerNotPermission(NotPermissionException e) {

        log.error("缺少权限: {}", e.getPermission());
        return SaResult.code(403).setMsg("缺少权限：" + e.getPermission());
    }

    // 缺少角色
    @ExceptionHandler(NotRoleException.class)
    public SaResult handlerNotRole(NotRoleException e) {

        log.error("缺少角色: {}", e.getRole());
        return SaResult.code(403).setMsg("缺少角色：" + e.getRole());
    }

    // 文件限制
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<String> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {

        log.error("文件大小超过限制，请上传小于30MB的文件");
        return ResponseEntity
                .status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body("文件大小超过限制，请上传小于30MB的文件");
    }

    // 参数校验
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolation(ConstraintViolationException e) {
        String errors = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));

        log.error("参数验证异常: {}", errors);
        return ResponseEntity.badRequest().body(
                Map.of("code", 400, "message", "参数验证失败: " + errors)
        );
    }

    // 参数校验失败
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationException(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.badRequest().body(
                Map.of("code", 400, "message", "参数校验失败", "errors", errors)
        );
    }

    // 邮件发送异常
    @ExceptionHandler(MailException.class)
    public ResponseEntity<?> handleMailException(MailException e) {
        log.error("邮件发送异常: {}", e.getMessage());
        return ResponseEntity.badRequest().body(
                Map.of("code", 500, "message", "邮件发送失败: " + e.getMessage())
        );
    }

    // 业务异常（用户可感知的校验失败，如滑块验证失败）
    @ExceptionHandler(BusinessException.class)
    public SaResult handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return SaResult.code(e.getCode()).setMsg(e.getMessage());
    }

    // 系统异常
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e) {
        log.error("系统异常: {}", e.getMessage(), e);
        return ResponseEntity.internalServerError().body(
                Map.of("code", 500, "message", "系统内部错误: " + e.getMessage())
        );
    }

}
