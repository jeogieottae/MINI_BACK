package com.example.mini.global.api.exception;

import com.example.mini.global.api.ApiResponse;
import com.example.mini.global.api.exception.error.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ExceptionAdvice {

    @ExceptionHandler(GlobalException.class)
    public ResponseEntity<ApiResponse<Object>> handleException(GlobalException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        log.warn("예외 발생 : {}", errorCode.getInfo());
        return ResponseEntity.status(errorCode.getCode()).body(ApiResponse.ERROR(errorCode));
    }
}