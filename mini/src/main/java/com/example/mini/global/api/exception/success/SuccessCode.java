package com.example.mini.global.api.exception.success;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SuccessCode {

	OK(HttpStatus.OK.value(), 200, "성공"),
	CREATED(HttpStatus.CREATED.value(), 201, "등록 성공"),
	DELETE(HttpStatus.OK.value(), 202, "삭제 성공"),
	;

	private final Integer HttpStatusCode;
	private final Integer successCode;
	private final String description;
}