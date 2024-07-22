package com.example.mini.global.redis;

import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CacheService {

	private RedisTemplate<String, Object> redisTemplate;

	private static final String LIKE_KEY_PREFIX = "like::";

	public void cacheLikeStatus(Long memberId, Long accomodationId, boolean isLiked) {
		String key = LIKE_KEY_PREFIX + memberId + "::" + accomodationId;
		redisTemplate.opsForValue().set(key, isLiked, 1, TimeUnit.HOURS); // 1시간 TTL
	}

	public Boolean getLikeStatus(Long memberId, Long accomodationId) { // redis에서 데이터 조회
		String key = LIKE_KEY_PREFIX + memberId + "::" + accomodationId;
		return (Boolean) redisTemplate.opsForValue().get(key);
	}

	public void evictLikeStatus(Long memberId, Long accomodationId) {
		String key = LIKE_KEY_PREFIX + memberId + "::" + accomodationId;
		redisTemplate.delete(key);
	}
}
