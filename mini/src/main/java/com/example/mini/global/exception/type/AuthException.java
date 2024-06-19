package com.example.mini.global.exception.type;

import com.example.mini.global.exception.error.ErrorCode;
import org.springframework.web.client.HttpStatusCodeException;

public class AuthException extends HttpStatusCodeException {
    public AuthException(ErrorCode errorCode) {
        super(errorCode.getCode(), errorCode.getInfo());
    }
}
