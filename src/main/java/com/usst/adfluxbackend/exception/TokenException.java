package com.usst.adfluxbackend.exception;

import lombok.Getter;

/**
 * 自定义Token异常类
 */
@Getter
public class TokenException extends RuntimeException {

    /**
     * 错误码
     */
    private final int code;

    public TokenException(int code, String message) {
        super(message);
        this.code = code;
    }

    public TokenException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public TokenException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }

}