package com.example.mini.global.exception;

import com.example.mini.global.exception.type.ReservationException;
import com.example.mini.global.util.APIUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ExceptionAdvice {
  @ExceptionHandler(ReservationException.class)
  public ResponseEntity ReservationException(ReservationException ex) {
    return APIUtil.ERROR(ex);
  }
}