package com.example.mini.global.exception;

import com.example.mini.global.exception.type.GlobalException;
import com.example.mini.global.util.api.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ExceptionAdvice {

    @ExceptionHandler(GlobalException.class)
    public ApiResponse<Object> handleAuthException(GlobalException ex) {
        return ApiResponse.ERROR(ex.getErrorCode());
    }
}