package com.example.mini.global.api.exception.error;

import org.springframework.http.HttpStatus;

public enum ReservationErrorCode implements ErrorCode {

  RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "예약을 찾을 수 없습니다."),
  NO_ROOMS_AVAILABLE(HttpStatus.BAD_REQUEST, "제공된 객실 ID에 해당하는 객실이 없습니다."),
  MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."),
  OVERLAPPING_RESERVATION(HttpStatus.BAD_REQUEST, "중복된 예약이 있습니다."),
  ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "객실을 찾을 수 없습니다."),
  EXCEEDS_MAX_GUESTS(HttpStatus.CONFLICT, "예약 인원이 최대 인원을 초과합니다"),
  INVALID_CHECKOUT_DATE(HttpStatus.CONFLICT, "체크아웃 시간이 체크인 시간을 앞설 수 없습니다."),
  CONFLICTING_RESERVATION(HttpStatus.CONFLICT, "해당 기간에는 예약하실 수 없습니다"),
  DUPLICATED_RESERVATION(HttpStatus.BAD_REQUEST, "이미 예약된 항목입니다"),
  INVALID_RESERVATION_STATUS(HttpStatus.BAD_REQUEST, "유효하지 않은 예약 상태입니다");

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

  @Override
  public String getCodeName() {
    return this.name();
  }
}
