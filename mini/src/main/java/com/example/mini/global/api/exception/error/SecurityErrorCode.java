package com.example.mini.global.api.exception.error;

import org.springframework.http.HttpStatus;

public enum SecurityErrorCode implements ErrorCode{

  FORBIDDEN(HttpStatus.FORBIDDEN, "접근이 거부됨"),
  UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 불가능함");

  private final HttpStatus code;
  private final String info;

  SecurityErrorCode(HttpStatus code, String info) {
    this.code = code;
    this.info = info;
  }

  public HttpStatus getCode() {
    return code;
  }

  public String getInfo() {
    return info;
  }

  @Override
  public String getCodeName() {
    return this.name();
  }
}
