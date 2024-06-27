package com.example.mini.global.security.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

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

	// 레디스 실행 확인용 (나중에 쓸거임)
   /*	public void deleteRefreshToken(String username) {
		redisTemplate.delete(username);
	}

	public boolean isTokenPresent(String username) {
		return redisTemplate.hasKey(username);
	}*/

	public void blacklistToken(String token) {
		redisTemplate.opsForValue().set(BLACKLIST_PREFIX + token, "true", Duration.ofDays(1)); // 토큰을 하루 동안 블랙리스트에 추가 , 하루 뒤에 Redis에서 제거
	}

	public boolean isTokenBlacklisted(String token) {
		return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + token));
	}

	public void removeToken(String token) {
		redisTemplate.delete(token);
	}
}
