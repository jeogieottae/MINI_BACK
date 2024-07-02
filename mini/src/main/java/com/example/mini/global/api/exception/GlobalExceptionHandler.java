package com.example.mini.global.api.exception;

import com.example.mini.global.api.ApiResponse;
import com.example.mini.global.api.exception.error.ErrorCode;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GlobalException.class)
    public ResponseEntity<ApiResponse<Object>> handleException(GlobalException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        log.warn("예외 발생 : {}", errorCode.getInfo());
        return ResponseEntity.status(errorCode.getCode()).body(ApiResponse.ERROR(errorCode));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.toList());
        log.warn("유효성 검사 예외 발생 : {}", errors);
        return ResponseEntity.badRequest().body(ApiResponse.VALIDATION_ERROR(errors));
    }
}