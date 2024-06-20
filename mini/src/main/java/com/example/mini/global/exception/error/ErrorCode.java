package com.example.mini.global.exception.error;
import org.springframework.http.HttpStatus;

public interface ErrorCode {

	HttpStatus getHttpStatus();

	Integer getErrorCode();

	String getDescription();
}
