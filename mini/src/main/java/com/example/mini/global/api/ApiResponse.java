package com.example.mini.global.api;

import com.example.mini.global.api.exception.error.ErrorCode;
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
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private T body;

	public static <T> ApiResponse<T> OK(T data) {
		ApiResponse<T> apiResponse = new ApiResponse<>();
		apiResponse.result = Result.OK();
		apiResponse.body = data;
		return apiResponse;
	}

	public static <T> ApiResponse<T> CREATED(T data) {
		ApiResponse<T> apiResponse = new ApiResponse<>();
		apiResponse.result = Result.CREATED();
		apiResponse.body = data;
		return apiResponse;
	}

	public static <T> ApiResponse<T> DELETE() {
		ApiResponse<T> apiResponse = new ApiResponse<>();
		apiResponse.result = Result.DELETE();
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
