package com.example.mini.global.redis;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RedisController {

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	@GetMapping("/get-token")
	public String getToken(@RequestParam String email) {
		String accessTokenKey = "accessToken:" + email;
		String refreshTokenKey = "refreshToken:" + email;

		Object accessToken = redisTemplate.opsForValue().get(accessTokenKey);
		Object refreshToken = redisTemplate.opsForValue().get(refreshTokenKey);

		return "AccessToken: " + (accessToken != null ? accessToken.toString() : "Not found") +
			", RefreshToken: " + (refreshToken != null ? refreshToken.toString() : "Not found");
	}
}