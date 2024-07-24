package com.example.mini.global.redis;

import com.example.mini.domain.like.entity.Like;
import com.example.mini.domain.like.repository.LikeRepository;
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

	public Boolean getLikeStatus(Long memberId, Long accomodationId) {
		String key = LIKE_KEY_PREFIX + memberId + "::" + accomodationId;
		return (Boolean) redisTemplate.opsForValue().get(key);
	}

	public void evictLikeStatus(Long memberId, Long accomodationId) {
		String key = LIKE_KEY_PREFIX + memberId + "::" + accomodationId;
		redisTemplate.delete(key);
	}

	/*캐시를 먼저 조회하고 없으면 DB 조회*/
	public Boolean readThroughLikeStatus(Long memberId, Long accomodationId, LikeRepository likeRepository) {
		String key = LIKE_KEY_PREFIX + memberId + "::" + accomodationId;
		Boolean cachedLikeStatus = (Boolean) redisTemplate.opsForValue().get(key);
		if (cachedLikeStatus != null) {
			return cachedLikeStatus;
		}

		Like like = likeRepository.findByMemberIdAndAccomodationId(memberId, accomodationId).orElse(null);
		if (like == null) {
			return false;
		}

		boolean isLiked = like.isLiked();
		redisTemplate.opsForValue().set(key, isLiked, 1, TimeUnit.HOURS);
		return isLiked;
	}
}
