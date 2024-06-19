package com.example.mini.global.util.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SuccessCode {

	OK(HttpStatus.OK, 200000, "성공"),
	CREATED(HttpStatus.CREATED, 201000, "등록 성공"),
	DELETED(HttpStatus.OK, 200001, "삭제 성공"),
	;
	;

	private final HttpStatus HttpStatusCode;
	private final Integer successCode; // 서버 자체 성공 코드
	private final String description;
}
