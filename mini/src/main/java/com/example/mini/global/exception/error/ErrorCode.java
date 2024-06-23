package com.example.mini.global.exception.error;
import org.springframework.http.HttpStatusCode;

public interface ErrorCode {

	HttpStatusCode getCode();

	String getInfo();
}
