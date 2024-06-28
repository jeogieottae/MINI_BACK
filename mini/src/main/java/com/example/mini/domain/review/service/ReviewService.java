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
import java.util.List;

import com.example.mini.global.model.dto.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

    private final int pageSize = 10;

    // 리뷰 생성
    public ReviewResponse addReview(Long memberId, ReviewRequest request) {
        Member member = getMember(memberId);
        Accomodation accomodation = getValidAccomodation(request.getAccomodationId());
        Reservation confirmedReservation = reservationRepository.findByMemberIdAndAccomodationIdAndStatus(
                        memberId, request.getAccomodationId(), ReservationStatus.CONFIRMED)
                .orElseThrow(() -> new GlobalException(ReviewErrorCode.RESERVATION_NOT_FOUND)); // 예약 상태 확인

        // 예약의 체크아웃 시간
        LocalDateTime memberCheckoutDate = getMemberCheckoutDate(memberId, accomodation.getId());
        // 리뷰 내용 검증
        validateReviewRequest(request, memberCheckoutDate, confirmedReservation);

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

    // 리뷰 조회
    public PagedResponse<AccomodationReviewResponse> getReviewsByAccomodationId(Long accomodationId, int page) {
        Accomodation accomodation = getValidAccomodation(accomodationId);
        Page<Review> reviewPage = reviewRepository.findByAccomodationOrderByCreatedAtDesc(accomodation, PageRequest.of(page-1, pageSize));
        List<AccomodationReviewResponse> content = reviewPage.stream().map(AccomodationReviewResponse::toDto).toList();
        return new PagedResponse<>(reviewPage.getTotalPages(), reviewPage.getTotalElements(), content);
    }

    // 리뷰 내용 검증
    private void validateReviewRequest(ReviewRequest request, LocalDateTime memberCheckoutDate, Reservation confirmedReservation) {
        // 별점이 비어있거나 이상한 값을 가지고 있음
        if (request.getStar() == null || request.getStar() < 1 || request.getStar() > 5) {
            throw new GlobalException(ReviewErrorCode.INVALID_REVIEW_STAR);
        }

        // 코멘트가 비어있음
        if (request.getComment() == null || request.getComment().isEmpty()) {
            throw new GlobalException(ReviewErrorCode.EMPTY_REVIEW_COMMENT);
        }

        // 현 시점이 체크아웃 이전
        if (LocalDateTime.now().isBefore(memberCheckoutDate)) {
            throw new GlobalException(ReviewErrorCode.INVALID_REVIEW_DATE);
        }

        // 예약에 대해서 리뷰를 작성한 이력이 있는지
        if (reviewRepository.existsByReservation(confirmedReservation)) {
            throw new GlobalException(ReviewErrorCode.DUPLICATE_REVIEW);
        }
    }

    // 회원정보 조회
    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new GlobalException(ReviewErrorCode.MEMBER_NOT_FOUND));
    }

    // 존재하는 숙소 id인지 확인, 숙소 객체 반환
    private Accomodation getValidAccomodation(Long accomodationId) {
        return accomodationRepository.findById(accomodationId)
                .orElseThrow(() -> new GlobalException(ReviewErrorCode.ACCOMODATION_NOT_FOUND));
    }

    // 예약의 체크아웃 정보 반환
    private LocalDateTime getMemberCheckoutDate(Long memberId, Long accomodationId) {
        Reservation confirmedReservation = reservationRepository.findByMemberIdAndAccomodationIdAndStatus(memberId, accomodationId, ReservationStatus.CONFIRMED)
                .orElseThrow(() -> new GlobalException(ReviewErrorCode.RESERVATION_NOT_FOUND));
        return confirmedReservation.getCheckOut();
    }
}