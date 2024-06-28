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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class LikeService {

  private final LikeRepository likeRepository;
  private final MemberRepository memberRepository;
  private final AccomodationRepository accomodationRepository;

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
  public Page<AccomodationResponse> getLikedAccomodations(Long memberId, Pageable pageable) {
    Page<Like> likes = likeRepository.findByMemberIdAndIsLiked(memberId, true, pageable);
    return likes.map(like -> {
      Accomodation accomodation = like.getAccomodation();
      return AccomodationResponse.builder()
          .name(accomodation.getName())
          .description(accomodation.getDescription())
          .postalCode(accomodation.getPostalCode())
          .address(accomodation.getAddress())
          .build();
    });
  }
}
