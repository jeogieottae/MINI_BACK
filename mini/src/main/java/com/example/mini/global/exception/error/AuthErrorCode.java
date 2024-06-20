package com.example.mini.global.exception.error;

import org.springframework.http.HttpStatus;

public enum AuthErrorCode implements ErrorCode {
	EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, 409001, "이미 존재하는 이메일입니다."),
	NAME_ALREADY_EXISTS(HttpStatus.CONFLICT, 409002, "이미 존재하는 이름입니다."),
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, 404001, "사용자를 찾을 수 없습니다."),
	PASSWORD_MISMATCH(HttpStatus.UNAUTHORIZED, 401001, "비밀번호가 일치하지 않습니다."),
	INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, 401002, "유효하지 않은 리프레시 토큰입니다."),
	INVALID_TOKEN(HttpStatus.UNAUTHORIZED, 401003, "유효하지 않은 토큰입니다."),
	INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, 401004, "유효하지 않은 액세스 토큰입니다."),
	BLACKLISTED_TOKEN(HttpStatus.UNAUTHORIZED, 401005, "블랙리스트에 등록된 토큰입니다.");

	private final HttpStatus HttpStatusCode;
	private final Integer errorCode; // 서버 자체 성공 코드
	private final String description;

	AuthErrorCode(HttpStatus HttpStatusCode, Integer errorCode, String description) {
		this.HttpStatusCode = HttpStatusCode;
		this.errorCode = errorCode;
		this.description = description;
	}

	@Override
	public HttpStatus getHttpStatus() {
		return HttpStatusCode;
	}

	@Override
	public Integer getErrorCode() {
		return errorCode;
	}

	@Override
	public String getDescription() {
		return description;
	}
}