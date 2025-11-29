package com.usst.adfluxbackend.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import com.usst.adfluxbackend.common.BaseResponse;
import com.usst.adfluxbackend.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 未登录 -> 返回 HTTP 401
     */
    @ExceptionHandler(NotLoginException.class)
    public ResponseEntity<BaseResponse<?>> notLoginException(NotLoginException e) {
        log.error("NotLoginException", e);
        // 构造响应体
        BaseResponse<?> response = ResultUtils.error(ErrorCode.NOT_LOGIN_ERROR, e.getMessage());
        // 返回 ResponseEntity：状态码 401 + 响应体
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }


    /**
     * Token异常 -> 返回 HTTP 401
     */
    @ExceptionHandler(TokenException.class)
    public ResponseEntity<BaseResponse<?>> tokenException(TokenException e) {
        log.error("TokenException", e);
        BaseResponse<?> response = ResultUtils.error(e.getCode(), e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    /**
     * 无权限 -> 返回 HTTP 403
     */
    @ExceptionHandler(NotPermissionException.class)
    public ResponseEntity<BaseResponse<?>> notPermissionExceptionHandler(NotPermissionException e) {
        log.error("NotPermissionException", e);
        BaseResponse<?> response = ResultUtils.error(ErrorCode.NO_AUTH_ERROR, e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    /**
     * 业务异常 -> 建议返回 HTTP 400 (Bad Request) 或者根据业务码动态判断
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<BaseResponse<?>> businessExceptionHandler(BusinessException e) {
        log.error("BusinessException", e);
        BaseResponse<?> response = ResultUtils.error(e.getCode(), e.getMessage());

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * 系统未知异常 -> 返回 HTTP 500
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<BaseResponse<?>> runtimeExceptionHandler(RuntimeException e) {
        log.error("RuntimeException", e);
        BaseResponse<?> response = ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统错误");
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}