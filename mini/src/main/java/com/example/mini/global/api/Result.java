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

	private Integer resultCode;
	private String resultMessage;

	public static Result OK() {
		return Result.builder()
			.resultCode(SuccessCode.OK.getSuccessCode())
			.resultMessage(SuccessCode.OK.getDescription())
			.build();
	}

	public static Result CREATED() {
		return Result.builder()
			.resultCode(SuccessCode.CREATED.getSuccessCode())
			.resultMessage(SuccessCode.CREATED.getDescription())
			.build();
	}

	public static Result DELETE() {
		return Result.builder()
			.resultCode(SuccessCode.DELETED.getSuccessCode())
			.resultMessage(SuccessCode.DELETED.getDescription())
			.build();
	}

	public static Result ERROR(ErrorCode errorCode) {
		return Result.builder()
			.resultCode(errorCode.getErrorCode())
			.resultMessage(errorCode.getDescription())
			.build();
	}

	public static Result VALIDATION_ERROR(List<String> errors) {
		return Result.builder()
			.resultCode(HttpStatus.BAD_REQUEST.value())
			.resultMessage(String.join(", ", errors))
			.build();
	}
}