package com.example.mini.global.api.exception.error;

import org.springframework.http.HttpStatus;

public enum CartErrorCode implements ErrorCode {

  CART_NOT_FOUND(HttpStatus.NOT_FOUND, "장바구니를 찾을 수 없습니다."),
  ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "객실을 찾을 수 없습니다.");

  private final HttpStatus code;
  private final String info;

  CartErrorCode(HttpStatus code, String info) {
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
