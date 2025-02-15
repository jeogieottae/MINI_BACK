package com.example.mini.global.api.exception.error;

import org.springframework.http.HttpStatus;

public enum AccomodationErrorCode implements ErrorCode{
    PAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 페이지 번호입니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),
    INVALID_ROOM_REQUEST(HttpStatus.BAD_REQUEST, "숙소에 해당 객실이 존재하지 않습니다."),
    INVALID_CATEGORY_CODE_REQUEST(HttpStatus.BAD_REQUEST, "요청 카테고리 값이 유효하지 않습니다.")
    ;

    private final HttpStatus code;
    private final String info;

    AccomodationErrorCode(HttpStatus code, String info) {
        this.code = code;
        this.info = info;
    }

    @Override
    public HttpStatus getCode() {
        return code;
    }

    @Override
    public String getInfo() {
        return info;
    }

    @Override
    public String getCodeName() {
        return this.name();
    }
}
