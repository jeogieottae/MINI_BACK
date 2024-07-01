package com.example.mini.global.api;

import com.example.mini.global.api.exception.error.ErrorCode;
import com.example.mini.global.api.exception.success.SuccessCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

	private Result result;
	@Valid
	private T body;

	// 데이터가 있는 경우
	public static <T> ApiResponse<T> SUCCESS(SuccessCode successCode, T data) {
		ApiResponse<T> apiResponse = new ApiResponse<>();
		apiResponse.result = Result.SUCCESS(successCode);
		apiResponse.body = data;
		return apiResponse;
	}

	// 데이터가 없는 경우
	public static <T> ApiResponse<T> SUCCESS(SuccessCode successCode) {
		ApiResponse<T> apiResponse = new ApiResponse<>();
		apiResponse.result = Result.SUCCESS(successCode);
		return apiResponse;
	}

	public static ApiResponse<Object> ERROR(ErrorCode errorCode) {
		ApiResponse apiResponse = new ApiResponse<Object>();
		apiResponse.result = Result.ERROR(errorCode);
		return apiResponse;
	}

	public static ApiResponse<Object> VALIDATION_ERROR(List<String> errors) {
		ApiResponse<Object> apiResponse = new ApiResponse<>();
		apiResponse.result = Result.VALIDATION_ERROR(errors);
		return apiResponse;
	}
}