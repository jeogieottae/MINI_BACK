package com.example.mini.global.api.exception.error;

import org.springframework.http.HttpStatus;

public enum ReviewErrorCode implements ErrorCode{
  MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자 정보를 찾을 수 없습니다"),
  ACCOMODATION_NOT_FOUND(HttpStatus.NOT_FOUND, "숙소 정보를 찾을 수 없습니다"),
  RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "예약 정보를 찾을 수 없습니다"),
  INVALID_REVIEW_DATE(HttpStatus.BAD_REQUEST, "유효하지 않은 리뷰 작성 기간입니다"),
  INVALID_REVIEW_STAR(HttpStatus.BAD_REQUEST, "유효하지 않은 별점입니다"),
  EMPTY_REVIEW_COMMENT(HttpStatus.BAD_REQUEST, "리뷰란이 비어있습니다"),
  DUPLICATE_REVIEW(HttpStatus.BAD_REQUEST, "이미 리뷰를 작성하셨습니다");

  private final HttpStatus code;
  private final String info;

  ReviewErrorCode(HttpStatus code, String info) {
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
