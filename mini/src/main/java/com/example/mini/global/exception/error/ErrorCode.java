package com.example.mini.global.exception.error;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public interface ErrorCode {

	HttpStatus getHttpStatus();

	Integer getErrorCode();

	String getDescription();
}
