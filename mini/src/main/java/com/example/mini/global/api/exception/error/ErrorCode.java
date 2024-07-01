package com.example.mini.global.api.exception.error;
import org.springframework.http.HttpStatusCode;

public interface ErrorCode {

	HttpStatusCode getCode();

	String getInfo();

	String getCodeName();


}
