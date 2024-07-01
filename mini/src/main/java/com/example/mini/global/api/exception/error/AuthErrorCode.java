package com.example.mini.global.api.exception.error;

import org.springframework.http.HttpStatus;

public enum AuthErrorCode implements ErrorCode {
	EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 이메일입니다."),
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
	AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "인가 코드를 받을 수 없습니다."),
	PASSWORD_MISMATCH(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다."),
	INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED,  "유효하지 않은 리프레시 토큰입니다."),
	INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
	INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 액세스 토큰입니다."),
	BLACKLISTED_TOKEN(HttpStatus.UNAUTHORIZED, "블랙리스트에 등록된 토큰입니다."),
	REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "리프레쉬 토큰을 찾을 수 없습니다"),
	NICKNAME_ALREADY_EXISTS(HttpStatus.UNAUTHORIZED, "이미 존재하는 닉네임입니다."),
	TOKEN_FETCH_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "토큰 요청에 실패했습니다.");

	private final HttpStatus code;
	private final String info;

	AuthErrorCode(HttpStatus code, String info) {
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