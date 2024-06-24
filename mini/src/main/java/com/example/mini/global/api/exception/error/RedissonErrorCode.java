package com.example.mini.global.api.exception.error;

import org.springframework.http.HttpStatus;

public enum RedissonErrorCode implements ErrorCode {

  KEY_NOT_GAIN(HttpStatus.NOT_FOUND, "키에 대한 락을 획득할 수 없습니다"),
  KEY_INTERRUPTED(HttpStatus.CONFLICT, "키 획득 중 인터럽트 발생했습니다"),
  QUEUE_ERROR(HttpStatus.CONFLICT, "큐에 대한 에러가 발생했습니다"),
  QUEUE_DATA_NOT_ADDED(HttpStatus.CONFLICT, "큐에 데이터가 저장되는 부분에서 에러가 발생했습니다");

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
