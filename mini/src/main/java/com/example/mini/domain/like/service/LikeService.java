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

  @Transactional
  public boolean toggleLike(Long memberId, Long accomodationId) {
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new GlobalException(LikeErrorCode.MEMBER_NOT_FOUND));

    Accomodation accomodation = accomodationRepository.findById(accomodationId)
        .orElseThrow(() -> new GlobalException(LikeErrorCode.ACCOMODATION_NOT_FOUND));

    Like like = likeRepository.findByMemberIdAndAccomodationId(memberId, accomodationId)
        .orElseGet(() -> new Like(member, accomodation, false));

    like.setLiked(!like.isLiked());
    likeRepository.save(like);

    return like.isLiked();
  }


  @Transactional(readOnly = true)
  public PagedResponse<AccomodationResponse> getLikedAccomodations(Long memberId, int page) {
    int pageSize = 10;
    Page<Like> likes = likeRepository.findByMemberIdAndIsLiked(memberId, true, PageRequest.of(page-1,
        pageSize));

    List<AccomodationResponse> content = likes.stream().map(like -> AccomodationResponse.toDto(like.getAccomodation())).toList();
    return new PagedResponse<>(likes.getTotalPages(), likes.getTotalElements(), content);
  }
}
