package com.example.mini.global.redis;

import com.example.mini.global.security.jwt.TokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class TokenServiceTest {

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	@Autowired
	private TokenService tokenService;


	@Test
	@DisplayName("리프레시 토큰 저장 후 가져오기")
	void saveAndGetRefreshToken() {
		// Given
		String username = "testUser";
		String refreshToken = "testRefreshToken";

		// When
		tokenService.saveRefreshToken(username, refreshToken);

		// Then
		String fetchedToken = tokenService.getRefreshToken(username);
		assertThat(fetchedToken).isEqualTo(refreshToken);
	}

	@Test
	@DisplayName("블랙리스트 토큰 확인")
	void blacklistAndCheckToken() {
		// Given
		String token = "testToken";

		// When
		tokenService.blacklistToken(token);

		// Then
		boolean isBlacklisted = tokenService.isTokenBlacklisted(token);
		assertThat(isBlacklisted).isTrue();

		// TTL 확인
		Long ttl = redisTemplate.getExpire("blacklist:" + token, TimeUnit.SECONDS);
		assertThat(ttl).isNotNull();
		assertThat(ttl).isGreaterThan(0);
	}

	@Test
	@DisplayName("토큰 제거")
	void removeToken() {
		// Given
		String token = "testToken";
		redisTemplate.opsForValue().set(token, "someValue", Duration.ofMinutes(5));

		// When
		tokenService.removeToken(token);

		// Then
		String value = (String) redisTemplate.opsForValue().get(token);
		assertThat(value).isNull();
	}

	@Test
	@DisplayName("하루 뒤에 블랙리스트에 등록된 토큰 사라지는지 확인")
	void tokenExpiresAfterOneDay() throws InterruptedException {
		// Given
		String token = "testTokenExpiry";
		tokenService.blacklistToken(token);

		// When
		boolean isBlacklisted = tokenService.isTokenBlacklisted(token);
		assertThat(isBlacklisted).isTrue();

		// 토큰을 수동으로 만료 (1일 지난 것으로 설정)
		redisTemplate.expire("blacklist:" + token, 1, TimeUnit.SECONDS);
		Thread.sleep(1100); // 키가 만료되도록 1초보다 조금 더 기다림

		// Then
		boolean isStillBlacklisted = tokenService.isTokenBlacklisted(token);
		assertThat(isStillBlacklisted).isFalse();
	}
}
