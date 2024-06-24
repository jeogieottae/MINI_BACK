package com.example.mini.global.api.exception.error;

import org.springframework.http.HttpStatus;

public enum RedissonErrorCode implements ErrorCode {

  KEY_NOT_GAIN(HttpStatus.NOT_FOUND, "키에 대한 락을 획득할 수 없습니다"),
  KEY_INTERRUPTED(HttpStatus.CONFLICT, "키 획득 중 인터럽트 발생했습니다");

  private final HttpStatus code;
  private final String info;

  RedissonErrorCode(HttpStatus code, String info) {
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
}
