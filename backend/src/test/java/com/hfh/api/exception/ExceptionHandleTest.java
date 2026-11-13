package com.hfh.api.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.dev33.satoken.util.SaResult;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mail.MailException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExceptionHandleTest {

    private final ExceptionHandle handle = new ExceptionHandle();

    @Test
    void notLoginShouldDistinguishTypes() {
        NotLoginException timeout = NotLoginException.newInstance("web", NotLoginException.TOKEN_TIMEOUT, "timeout", null);
        assertThat(handle.handlerNotLogin(timeout).getCode()).isEqualTo(401);
        assertThat(handle.handlerNotLogin(timeout).getMsg()).isEqualTo("accessToken 已过期");

        NotLoginException kick = NotLoginException.newInstance("web", NotLoginException.KICK_OUT, "kick", null);
        assertThat(handle.handlerNotLogin(kick).getMsg()).isEqualTo("已被踢下线");

        NotLoginException other = NotLoginException.newInstance("web", NotLoginException.NOT_TOKEN, "none", null);
        assertThat(handle.handlerNotLogin(other).getMsg()).isEqualTo("请先登录");
    }

    @Test
    void notPermissionShouldReturn403() {
        NotPermissionException e = mock(NotPermissionException.class);
        when(e.getPermission()).thenReturn("user:add");

        SaResult result = handle.handlerNotPermission(e);

        assertThat(result.getCode()).isEqualTo(403);
        assertThat(result.getMsg()).isEqualTo("缺少权限：user:add");
    }

    @Test
    void notRoleShouldReturn403() {
        NotRoleException e = mock(NotRoleException.class);
        when(e.getRole()).thenReturn("admin");

        SaResult result = handle.handlerNotRole(e);

        assertThat(result.getCode()).isEqualTo(403);
        assertThat(result.getMsg()).isEqualTo("缺少角色：admin");
    }

    @Test
    void uploadSizeLimitShouldReturn400() {
        SaResult result = handle.handleMaxUploadSizeExceeded(new MaxUploadSizeExceededException(1024L));

        assertThat(result.getCode()).isEqualTo(400);
        assertThat(result.getMsg()).contains("文件大小超过限制");
    }

    @Test
    void constraintViolationShouldJoinMessages() {
        ConstraintViolation<?> v1 = mock(ConstraintViolation.class);
        ConstraintViolation<?> v2 = mock(ConstraintViolation.class);
        when(v1.getMessage()).thenReturn("a");
        when(v2.getMessage()).thenReturn("b");

        SaResult result = handle.handleConstraintViolation(
                new ConstraintViolationException(Set.of(v1, v2)));

        assertThat(result.getCode()).isEqualTo(400);
        assertThat(result.getMsg()).contains("a").contains("b");
    }

    @Test
    void methodArgumentNotValidShouldReturnFirstMessage() {
        MethodArgumentNotValidException e = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(e.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors())
                .thenReturn(List.of(new FieldError("obj", "field", "不能为空")));

        assertThat(handle.handleValidationException(e).getMsg()).isEqualTo("不能为空");

        when(bindingResult.getFieldErrors()).thenReturn(List.of());
        assertThat(handle.handleValidationException(e).getMsg()).isEqualTo("参数校验失败");
    }

    @Test
    void bindExceptionShouldFallbackWhenMessageNull() {
        BindException e = mock(BindException.class);
        when(e.getAllErrors()).thenReturn(List.of(new FieldError("obj", "field", (String) null)));

        assertThat(handle.handleBindException(e).getMsg()).isEqualTo("参数校验失败");

        when(e.getAllErrors()).thenReturn(List.of(new FieldError("obj", "field", "错误")));
        assertThat(handle.handleBindException(e).getMsg()).isEqualTo("错误");

        when(e.getAllErrors()).thenReturn(List.of());
        assertThat(handle.handleBindException(e).getMsg()).isEqualTo("参数校验失败");
    }

    @Test
    void notReadableShouldReturnFormatError() {
        assertThat(handle.handleNotReadable(mock(HttpMessageNotReadableException.class)).getMsg())
                .isEqualTo("请求参数格式不正确");
    }

    @Test
    void mailExceptionShouldReturn500() {
        SaResult result = handle.handleMailException(mock(MailException.class));

        assertThat(result.getCode()).isEqualTo(500);
        assertThat(result.getMsg()).isEqualTo("邮件发送失败，请稍后重试");
    }

    @Test
    void businessExceptionShouldUseItsOwnCode() {
        SaResult result = handle.handleBusinessException(new BusinessException(422, "参数不合法"));

        assertThat(result.getCode()).isEqualTo(422);
        assertThat(result.getMsg()).isEqualTo("参数不合法");

        assertThat(handle.handleBusinessException(new BusinessException("普通错误")).getCode())
                .isEqualTo(400);
    }

    @Test
    void unknownExceptionShouldHideDetails() {
        SaResult result = handle.handleException(new IllegalStateException("secret"));

        assertThat(result.getCode()).isEqualTo(500);
        assertThat(result.getMsg()).isEqualTo("系统内部错误，请稍后重试");
    }
}
