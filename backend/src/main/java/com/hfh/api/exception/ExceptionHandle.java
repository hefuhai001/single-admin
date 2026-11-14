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
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mail.MailException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.stream.Collectors;

/**
 * 全局异常处理。
 * <p>
 * 响应体统一为 SaResult（{@code {code, msg, data}}），与正常接口保持同一结构；
 * 同时返回真实 HTTP 状态码：401 未登录、403 无权限、400 参数/业务错误、500 系统异常，
 * 便于网关、APM、负载均衡按状态码做告警与熔断。前端拦截器同时兼容状态码与响应体 code。
 * 业务异常携带自定义 code 时，响应体保留该 code，HTTP 状态统一为 400。
 * </p>
 */
@Hidden
@Slf4j
@RestControllerAdvice
public class ExceptionHandle {

    private static final int CODE_BAD_REQUEST = 400;
    private static final int CODE_UNAUTHORIZED = 401;
    private static final int CODE_FORBIDDEN = 403;
    private static final int CODE_SERVER_ERROR = 500;

    // 未登录
    @ExceptionHandler(NotLoginException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public SaResult handlerNotLogin(NotLoginException e) {
        // 区分 token 过期类型，前端据此判断是否可尝试刷新
        String message;
        if (NotLoginException.TOKEN_TIMEOUT.equals(e.getType())) {
            // accessToken 过期，前端可用 refreshToken 刷新
            message = "accessToken 已过期";
        } else if (NotLoginException.KICK_OUT.equals(e.getType())) {
            message = "已被踢下线";
        } else {
            message = "请先登录";
        }
        return SaResult.code(CODE_UNAUTHORIZED).setMsg(message);
    }

    // 缺少权限
    @ExceptionHandler(NotPermissionException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public SaResult handlerNotPermission(NotPermissionException e) {
        log.error("缺少权限: {}", e.getPermission());
        return SaResult.code(CODE_FORBIDDEN).setMsg("缺少权限：" + e.getPermission());
    }

    // 缺少角色
    @ExceptionHandler(NotRoleException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public SaResult handlerNotRole(NotRoleException e) {
        log.error("缺少角色: {}", e.getRole());
        return SaResult.code(CODE_FORBIDDEN).setMsg("缺少角色：" + e.getRole());
    }

    // 文件大小限制
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public SaResult handleMaxUploadSizeExceeded(MaxUploadSizeExceededException e) {
        log.warn("上传文件超过大小限制: {}", e.getMessage());
        return SaResult.code(CODE_BAD_REQUEST).setMsg("文件大小超过限制，请上传小于 30MB 的文件");
    }

    // 单个参数校验失败（如 @RequestParam 上的约束）
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public SaResult handleConstraintViolation(ConstraintViolationException e) {
        String errors = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数验证异常: {}", errors);
        return SaResult.code(CODE_BAD_REQUEST).setMsg("参数验证失败：" + errors);
    }

    // @RequestBody 参数校验失败
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public SaResult handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("参数校验失败");
        log.warn("参数校验失败: {}", message);
        return SaResult.code(CODE_BAD_REQUEST).setMsg(message);
    }

    // 查询参数绑定校验失败（如分页参数对象）
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public SaResult handleBindException(BindException e) {
        String message = e.getAllErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage() == null ? "参数校验失败" : error.getDefaultMessage())
                .orElse("参数校验失败");
        log.warn("参数绑定失败: {}", message);
        return SaResult.code(CODE_BAD_REQUEST).setMsg(message);
    }

    // 请求体格式错误
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public SaResult handleNotReadable(HttpMessageNotReadableException e) {
        log.warn("请求体解析失败: {}", e.getMessage());
        return SaResult.code(CODE_BAD_REQUEST).setMsg("请求参数格式不正确");
    }

    // 邮件发送异常
    @ExceptionHandler(MailException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public SaResult handleMailException(MailException e) {
        log.error("邮件发送异常: {}", e.getMessage(), e);
        return SaResult.code(CODE_SERVER_ERROR).setMsg("邮件发送失败，请稍后重试");
    }

    // 业务异常（用户可感知的校验失败，如滑块验证失败）
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public SaResult handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return SaResult.code(e.getCode()).setMsg(e.getMessage());
    }

    // 系统异常：仅记录服务端日志，响应中不返回异常细节，避免泄露内部信息
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public SaResult handleException(Exception e) {
        log.error("系统异常", e);
        return SaResult.code(CODE_SERVER_ERROR).setMsg("系统内部错误，请稍后重试");
    }

}
