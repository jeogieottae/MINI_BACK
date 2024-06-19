package com.example.mini.global.exception.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AccomodationErrorCode implements ErrorCode{
    PAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "유효하지 않은 페이지 번호입니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다.")
    ;

    private final HttpStatus code;
    private final String info;
}
