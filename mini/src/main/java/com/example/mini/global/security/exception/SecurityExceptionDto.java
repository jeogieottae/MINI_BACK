package com.example.mini.global.security.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SecurityExceptionDto {
	private int statusCode;
	private String msg;

	public SecurityExceptionDto(int statusCode, String msg) {
		this.statusCode = statusCode;
		this.msg = msg;
	}

	class ResponseMessage {
		public static final String FORBIDDEN = "접근이 거부됨";
		public static final String UNAUTHORIZED = "인증이 불가능함";
	}
}