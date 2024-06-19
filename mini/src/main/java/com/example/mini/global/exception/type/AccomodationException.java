package com.example.mini.global.exception.type;

import com.example.mini.global.exception.error.AccomodationErrorCode;
import org.springframework.web.client.HttpStatusCodeException;

public class AccomodationException extends HttpStatusCodeException {
    public AccomodationException(AccomodationErrorCode errorCode) {
        super(errorCode.getCode(), errorCode.getInfo());
    }
}
