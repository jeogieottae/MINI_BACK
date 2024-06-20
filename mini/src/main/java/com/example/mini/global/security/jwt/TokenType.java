package com.example.mini.global.security.jwt;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TokenType {
	ACCESS("ACCESS", 30 * 60 * 1000L), // 30 minutes
	REFRESH("REFRESH", 14 * 24 * 60 * 60 * 1000L); // 14 days

	private final String type;
	private final Long expireTime;
}