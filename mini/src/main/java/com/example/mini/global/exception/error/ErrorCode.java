package com.example.mini.global.exception.error;
import org.springframework.http.HttpStatusCode;

public interface ErrorCode {

	Integer getHttpStatusCode();

	Integer getErrorCode();

	String getDescription();

}
