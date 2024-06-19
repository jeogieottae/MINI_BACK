package com.example.mini.global.exception.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCodeImpl implements ErrorCode {
	SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), 1500, "Server Error"),
	;

	private final Integer HttpStatusCode;
	private final Integer errorCode; // 서버 자체 에러 코드
	private final String description;
}
