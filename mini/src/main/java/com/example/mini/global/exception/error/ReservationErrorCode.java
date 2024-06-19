package com.example.mini.global.exception.error;

import org.springframework.http.HttpStatus;

public enum ReservationErrorCode implements ErrorCode {

  RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "예약을 찾을 수 없습니다."),
  NO_ROOMS_AVAILABLE(HttpStatus.BAD_REQUEST, "제공된 객실 ID에 해당하는 객실이 없습니다.");

  private final HttpStatus code;
  private final String info;

  ReservationErrorCode(HttpStatus code, String info) {
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
