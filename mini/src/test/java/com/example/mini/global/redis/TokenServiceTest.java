package com.example.mini.global.redis;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.example.mini.global.security.jwt.TokenService;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@DataRedisTest
@Import(RedisConfig.class)
@EnableAutoConfiguration(exclude = JpaRepositoriesAutoConfiguration.class)
public class TokenServiceTest {

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	private TokenService tokenService;

	@BeforeEach
	void setUp() {
		tokenService = new TokenService(redisTemplate);
	}

	@Test
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
	void blacklistAndCheckToken() {
		// Given
		String token = "testToken";

		// When
		tokenService.blacklistToken(token);

		// Then
		boolean isBlacklisted = tokenService.isTokenBlacklisted(token);
		assertThat(isBlacklisted).isTrue();
	}

	@Test
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
}