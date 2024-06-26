package com.example.mini.global.api;

import com.example.mini.global.api.exception.error.ErrorCode;
import com.example.mini.global.api.exception.success.SuccessCode;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Result {
	private HttpStatusCode resultCode;
	private String resultMessage;
	private String resultDescription;
	public static Result OK() {
		return Result.builder()
				.resultCode(HttpStatus.OK)
				.resultMessage("성공")
				.resultDescription(SuccessCode.OK.getDescription())
				.build();
	}
	public static Result CREATED() {
		return Result.builder()
				.resultCode(HttpStatus.CREATED)
				.resultMessage("등록 성공")
				.resultDescription(SuccessCode.CREATED.getDescription())
				.build();
	}
	public static Result DELETE() {
		return Result.builder()
				.resultCode(HttpStatus.OK)
				.resultMessage("삭제성공")
				.resultDescription(SuccessCode.DELETE.getDescription())
				.build();
	}
	public static Result ERROR(ErrorCode errorCode) {
		return Result.builder()
				.resultCode(errorCode.getCode())
				.resultMessage("error")
				.resultDescription(errorCode.getInfo())
				.build();
	}
	public static Result ERROR(ErrorCode errorCode, String description) {
		return Result.builder()
				.resultCode(errorCode.getCode())
				.resultMessage("error")
				.resultDescription(description)
				.build();
	}
	public static Result VALIDATION_ERROR(List<String> errors) {
		return Result.builder()
				.resultCode(HttpStatus.BAD_REQUEST)
				.resultMessage("Validation Error")
				.resultDescription(String.join(", ", errors))
				.build();
	}
}