package com.example.mini.global.security.jwt;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {

	private final RedisTemplate<String, Object> redisTemplate;
	private static final String BLACKLIST_PREFIX = "blacklist:";

	public void saveRefreshToken(String username, String refreshToken) {
		redisTemplate.opsForValue().set(username, refreshToken, Duration.ofDays(14)); // 14일 동안 유효
	}

	public String getRefreshToken(String username) {
		return (String) redisTemplate.opsForValue().get(username);
	}

	public void deleteRefreshToken(String username) {
		redisTemplate.delete(username);
	}


	// 블랙리스트에 액세스 토큰 추가
	public void blacklistToken(String token) {
		redisTemplate.opsForValue().set(BLACKLIST_PREFIX + token, "true", Duration.ofDays(1)); // 토큰을 하루 동안 블랙리스트에 추가
	}

	// 블랙리스트에서 액세스 토큰 확인
	public boolean isTokenBlacklisted(String token) {
		return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + token));
	}
}
