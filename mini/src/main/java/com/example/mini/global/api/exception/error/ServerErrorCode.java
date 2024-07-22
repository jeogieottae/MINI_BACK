package com.example.mini.global.api.exception.error;

import org.springframework.http.HttpStatus;

public enum ServerErrorCode implements ErrorCode{

  SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error");

  private final HttpStatus code;
  private final String info;

  ServerErrorCode(HttpStatus code, String info) {
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