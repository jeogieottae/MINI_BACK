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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

  private final int pageSize = 10;

  // todo : addLike에 좋아요 취소하는 동작도 넣는게 맞는지 고민
  @Transactional
  public void addLike(Long memberId, Long accomodationId) {
    Member member = getMember(memberId);
    Accomodation accomodation = getAccomodation(accomodationId);

    likeRepository.findByMemberIdAndAccomodationId(memberId, accomodationId)
        .ifPresentOrElse(
            like -> {
              like.setLiked(!like.isLiked());
              },
            () -> likeRepository.save(new Like(
                member, accomodation, true
            ))
        );
  }

  private Member getMember(Long memberId) {
    return memberRepository.findById(memberId)
        .orElseThrow(() -> new GlobalException(LikeErrorCode.MEMBER_NOT_FOUND));
  }

  private Accomodation getAccomodation(Long accomodationId) {
    return accomodationRepository.findById(accomodationId)
        .orElseThrow(() -> new GlobalException(LikeErrorCode.ACCOMODATION_NOT_FOUND));
  }

  @Transactional(readOnly = true)
  public PagedResponse<AccomodationResponse> getLikedAccomodations(Long memberId, int page) {
    Page<Like> likes = likeRepository.findByMemberIdAndIsLiked(memberId, true, PageRequest.of(page-1, pageSize));

    List<AccomodationResponse> content = likes.stream().map(like -> AccomodationResponse.toDto(like.getAccomodation())).toList();
    return new PagedResponse<>(likes.getTotalPages(), likes.getTotalElements(), content);
  }
}
