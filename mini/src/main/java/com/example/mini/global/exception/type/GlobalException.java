package com.example.mini.global.exception.type;

import com.example.mini.global.exception.error.ErrorCode;
import org.springframework.web.client.HttpStatusCodeException;

public class GlobalException extends HttpStatusCodeException {
    private final ErrorCode errorCode;

    public GlobalException(ErrorCode errorCode) {
        super(errorCode.getHttpStatus(), errorCode.getDescription());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
