package com.hfh.api.exception;

import lombok.Getter;

/**
 * 业务异常
 * <p>
 * 用于业务逻辑校验失败（如滑块验证失败、验证码错误、用户已存在等），
 * 全局异常处理器会捕获并返回 400 状态码及具体错误信息，不记录为系统内部错误。
 * </p>
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(String message) {
        super(message);
        this.code = 400;
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
