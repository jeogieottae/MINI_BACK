package com.example.mini.domain.like.service;

import com.example.mini.domain.accomodation.entity.Accomodation;
import com.example.mini.domain.accomodation.repository.AccomodationRepository;
import com.example.mini.domain.like.entity.Like;
import com.example.mini.domain.like.model.response.AccomodationResponse;
import com.example.mini.domain.like.repository.LikeRepository;
import com.example.mini.domain.member.entity.Member;
import com.example.mini.domain.member.repository.MemberRepository;
import com.example.mini.global.api.exception.GlobalException;
import com.example.mini.global.api.exception.error.LikeErrorCode;
import com.example.mini.global.model.dto.PagedResponse;
import com.example.mini.global.redis.CacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class LikeService {

  private final LikeRepository likeRepository;
  private final MemberRepository memberRepository;
  private final AccomodationRepository accomodationRepository;
  private final CacheService cacheService;

  @Transactional
  public boolean toggleLike(Long memberId, Long accomodationId) {
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new GlobalException(LikeErrorCode.MEMBER_NOT_FOUND));

    Accomodation accomodation = accomodationRepository.findById(accomodationId)
        .orElseThrow(() -> new GlobalException(LikeErrorCode.ACCOMODATION_NOT_FOUND));

    Boolean currentStatus = getLikeStatus(memberId, accomodationId);
    boolean newStatus = !currentStatus;

    Like like = likeRepository.findByMemberIdAndAccomodationId(memberId, accomodationId)
        .orElseGet(() -> new Like(member, accomodation, newStatus));

    like.setLiked(newStatus);
    likeRepository.save(like);

    // 캐시 갱신
    if (newStatus) {
      cacheService.cacheLikeStatus(memberId, accomodationId, true);
    } else {
      cacheService.evictLikeStatus(memberId, accomodationId);
    }

    return newStatus;
  }

  @Transactional(readOnly = true)
  public PagedResponse<AccomodationResponse> getLikedAccomodations(Long memberId, int page) {
    int pageSize = 10;
    Page<Like> likes = likeRepository.findByMemberIdAndIsLiked(memberId, true, PageRequest.of(page - 1, pageSize));

    List<AccomodationResponse> content = likes.stream()
        .map(like -> AccomodationResponse.toDto(like.getAccomodation()))
        .toList();
    return new PagedResponse<>(likes.getTotalPages(), likes.getTotalElements(), content);
  }

  @Transactional(readOnly = true)
  public Boolean getLikeStatus(Long memberId, Long accomodationId) {
    Boolean cachedLikeStatus = cacheService.getLikeStatus(memberId, accomodationId);

    if (cachedLikeStatus != null) { // 캐시에 데이터가 있으면 캐시에서 반환
      return cachedLikeStatus;
    }

    // 캐시에 없는 경우 DB에서 조회
    Like like = likeRepository.findByMemberIdAndAccomodationId(memberId, accomodationId).orElse(null);
    if (like == null) {
      return false; // 좋아요가 없는 경우 false 반환
    }

    boolean isLiked = like.isLiked();
    cacheService.cacheLikeStatus(memberId, accomodationId, isLiked); // DB에서 조회한 데이터를 캐시에 저장
    return isLiked;
  }
}
