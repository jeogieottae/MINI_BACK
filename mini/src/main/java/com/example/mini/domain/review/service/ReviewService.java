package com.example.mini.domain.review.service;

import com.example.mini.domain.accomodation.entity.Accomodation;
import com.example.mini.domain.accomodation.repository.AccomodationRepository;
import com.example.mini.domain.member.entity.Member;
import com.example.mini.domain.member.repository.MemberRepository;
import com.example.mini.domain.reservation.entity.Reservation;
import com.example.mini.domain.reservation.entity.enums.ReservationStatus;
import com.example.mini.domain.reservation.repository.ReservationRepository;
import com.example.mini.domain.review.entity.Review;
import com.example.mini.domain.review.model.request.ReviewRequest;
import com.example.mini.domain.review.model.response.AccomodationReviewResponse;
import com.example.mini.domain.review.model.response.ReviewResponse;
import com.example.mini.domain.review.repository.ReviewRepository;
import com.example.mini.global.api.exception.GlobalException;
import com.example.mini.global.api.exception.error.ReviewErrorCode;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
@RequiredArgsConstructor
public class ReviewService {

  private final ReviewRepository reviewRepository;
  private final MemberRepository memberRepository;
  private final AccomodationRepository accomodationRepository;
  private final ReservationRepository reservationRepository;

  public ReviewResponse addReview(Long memberId, ReviewRequest request) {
    Member member = getMember(memberId);

    Accomodation accomodation = getValidAccomodation(request.getAccomodationId());

    Reservation confirmedReservation = reservationRepository.findByMemberIdAndAccomodationIdAndStatus(
            memberId, request.getAccomodationId(), ReservationStatus.CONFIRMED)
        .orElseThrow(() -> new GlobalException(ReviewErrorCode.RESERVATION_NOT_FOUND));

    LocalDateTime memberCheckoutDate = getMemberCheckoutDate(memberId, accomodation.getId());
    LocalDateTime serverCurrentTime = LocalDateTime.now();

    if (serverCurrentTime.isBefore(memberCheckoutDate)) {
      throw new GlobalException(ReviewErrorCode.INVALID_REVIEW_DATE);
    }

    if (reviewRepository.existsByReservation(confirmedReservation)) {
      throw new GlobalException(ReviewErrorCode.DUPLICATE_REVIEW);
    }

    validateReviewRequest(request);

    Review review = Review.builder()
        .comment(request.getComment())
        .star(request.getStar())
        .member(member)
        .accomodation(accomodation)
        .reservation(confirmedReservation)
        .build();

    reviewRepository.save(review);

    return new ReviewResponse(review.getComment(), review.getStar());
  }

  private void validateReviewRequest(ReviewRequest request) {
    if (request.getStar() == null || request.getStar() < 1 || request.getStar() > 5) {
      throw new GlobalException(ReviewErrorCode.INVALID_REVIEW_STAR);
    }

    if (request.getComment() == null || request.getComment().isEmpty()) {
      throw new GlobalException(ReviewErrorCode.EMPTY_REVIEW_COMMENT);
    }
  }

  private Member getMember(Long memberId) {
    return memberRepository.findById(memberId)
        .orElseThrow(() -> new GlobalException(ReviewErrorCode.MEMBER_NOT_FOUND));
  }

  private Accomodation getValidAccomodation(Long accomodationId) {
    return accomodationRepository.findById(accomodationId)
        .orElseThrow(() -> new GlobalException(ReviewErrorCode.ACCOMODATION_NOT_FOUND));
  }

  private LocalDateTime getMemberCheckoutDate(Long memberId, Long accomodationId) {
    Reservation confirmedReservation = reservationRepository.findByMemberIdAndAccomodationIdAndStatus(memberId, accomodationId, ReservationStatus.CONFIRMED)
        .orElseThrow(() -> new GlobalException(ReviewErrorCode.RESERVATION_NOT_FOUND));

    return confirmedReservation.getCheckOut();
  }


  public Page<AccomodationReviewResponse> getReviewsByAccomodationId(Long accomodationId, int page, int size) {
    Accomodation accomodation = getValidAccomodation(accomodationId);
    Pageable pageable = PageRequest.of(page, size);
    Page<Review> reviewPage = reviewRepository.findByAccomodationOrderByCreatedAtDesc(accomodation, pageable);

    return reviewPage.map(this::convertToAccomodationReviewResponse);
  }

  private AccomodationReviewResponse convertToAccomodationReviewResponse(Review review) {
    return AccomodationReviewResponse.builder()
            .comment(review.getComment())
            .star(review.getStar())
            .memberName(review.getMember().getName())
            .createdAt(review.getCreatedAt())
            .build();
  }
}

