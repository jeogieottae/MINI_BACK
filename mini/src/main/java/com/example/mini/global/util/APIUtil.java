package com.example.mini.global.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import com.example.mini.global.model.response.ErrorResponse;

@Slf4j
public class APIUtil {

	public static <T> ResponseEntity<T> OK(T data) {
		return ResponseEntity.ok().body(data);
	}

	public static ResponseEntity OK(String msg) {
		return ResponseEntity.ok().body(msg);
	}

	public static ResponseEntity OK() {
		return ResponseEntity.ok().build();
	}

	public static ResponseEntity ERROR(HttpStatusCodeException ex) {
		log.warn(ex.getStatusText());
		return ResponseEntity
			.status(ex.getStatusCode().value())
			.body(new ErrorResponse(ex.getStatusText()));
	}
}