package com.example.mini.global.api.exception.error;

import org.springframework.http.HttpStatus;

public enum CartErrorCode implements ErrorCode {

  CART_NOT_FOUND(HttpStatus.NOT_FOUND, "장바구니를 찾을 수 없습니다."),
  ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "객실을 찾을 수 없습니다."),
  MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다"),
  RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "예약 정보를 찾을 수 없습니다"),
  RESERVATION_NOT_IN_CART(HttpStatus.NOT_FOUND, "해당 예약이 장바구니 안에 없습니다"),
  EXCEEDS_MAX_GUESTS(HttpStatus.CONFLICT, "예약 인원이 최대 인원을 초과합니다"),
  DUPLICATE_RESERVATION(HttpStatus.CONFLICT, "이미 존재하는 장바구니 항목입니다"),
  RESERVATION_NOT_PENDING(HttpStatus.CONFLICT, "Pending상태가 아닌 항목입니다"),
  RESERVATION_NOT_BELONGS_TO_USER(HttpStatus.CONFLICT, "해당 사용자의 예약정보가 아닙니다"),
  INVALID_CHECKOUT_DATE(HttpStatus.CONFLICT, "체크아웃 시간이 체크인 시간을 앞설 수 없습니다."),
  CONFLICTING_RESERVATION(HttpStatus.CONFLICT, "해당 기간에는 예약하실 수 없습니다");

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
