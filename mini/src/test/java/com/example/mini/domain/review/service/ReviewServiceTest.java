package com.example.mini.domain.review.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.mini.domain.accomodation.entity.Accomodation;
import com.example.mini.domain.accomodation.entity.Room;
import com.example.mini.domain.accomodation.fixture.AccomodationEntityFixture;
import com.example.mini.domain.accomodation.repository.AccomodationRepository;
import com.example.mini.domain.member.entity.Member;
import com.example.mini.domain.member.fixture.MemberEntityFixture;
import com.example.mini.domain.member.repository.MemberRepository;
import com.example.mini.domain.reservation.entity.Reservation;
import com.example.mini.domain.reservation.entity.enums.ReservationStatus;
import com.example.mini.domain.reservation.fixture.ReservationEntityFixture;
import com.example.mini.domain.reservation.repository.ReservationRepository;
import com.example.mini.domain.review.entity.Review;
import com.example.mini.domain.review.model.request.ReviewRequest;
import com.example.mini.domain.review.model.response.ReviewResponse;
import com.example.mini.domain.review.repository.ReviewRepository;
import com.example.mini.global.api.exception.GlobalException;
import com.example.mini.global.api.exception.error.ReviewErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ReviewServiceTest {

  @Mock
  private ReviewRepository reviewRepository;

  @Mock
  private MemberRepository memberRepository;

  @Mock
  private AccomodationRepository accomodationRepository;

  @Mock
  private ReservationRepository reservationRepository;

  @InjectMocks
  private ReviewService reviewService;

  private Member member;
  private Accomodation accomodation;
  private Room room;
  private Reservation reservation;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    member = MemberEntityFixture.getMember();
    accomodation = AccomodationEntityFixture.getAccomodation();
    room = AccomodationEntityFixture.getRoom(accomodation);
    reservation = ReservationEntityFixture.getReservation(member, accomodation, room);

    when(memberRepository.findById(1L)).thenReturn(java.util.Optional.of(member));
    when(accomodationRepository.findById(1L)).thenReturn(java.util.Optional.of(accomodation));
    when(reservationRepository.findByMemberIdAndAccomodationIdAndStatus(1L, 1L, ReservationStatus.CONFIRMED)).thenReturn(java.util.Optional.of(reservation));
  }

  @Test
  void 리뷰_추가_유효한_요청_조건_충족() {
    // Given
    ReviewRequest request = ReviewRequest.builder()
        .accomodationId(1L)
        .comment("좋았습니다!")
        .star(5)
        .build();

    when(reviewRepository.existsByReservation(reservation)).thenReturn(false);

    // When
    ReviewResponse response = reviewService.addReview(1L, request);

    // Then
    verify(reviewRepository, times(1)).save(any(Review.class));
    assertEquals(request.getComment(), response.getComment());
    assertEquals(request.getStar(), response.getStar());
  }

  @Test
  void 리뷰_추가_예약_없음_예외_발생() {
    // Given
    ReviewRequest request = ReviewRequest.builder()
        .accomodationId(1L)
        .comment("좋았습니다!")
        .star(5)
        .build();

    when(reservationRepository.findByMemberIdAndAccomodationIdAndStatus(1L, 1L, ReservationStatus.CONFIRMED)).thenReturn(java.util.Optional.empty());

    // When & Then
    GlobalException exception = assertThrows(GlobalException.class, () -> reviewService.addReview(1L, request));
    assertEquals(ReviewErrorCode.RESERVATION_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  void 리뷰_추가_리뷰_존재_예외_발생() {
    // Given
    ReviewRequest request = ReviewRequest.builder()
        .accomodationId(1L)
        .comment("좋았습니다!")
        .star(5)
        .build();

    when(reviewRepository.existsByReservation(reservation)).thenReturn(true);

    // When & Then
    GlobalException exception = assertThrows(GlobalException.class, () -> reviewService.addReview(1L, request));
    assertEquals(ReviewErrorCode.DUPLICATE_REVIEW, exception.getErrorCode());
  }

  @Test
  void 리뷰_추가_별점_유효하지_않음_예외_발생() {
    // Given
    ReviewRequest request = ReviewRequest.builder()
        .accomodationId(1L)
        .comment("좋았습니다!")
        .star(6) // 유효하지 않은 별점
        .build();

    // When & Then
    GlobalException exception = assertThrows(GlobalException.class, () -> reviewService.addReview(1L, request));
    assertEquals(ReviewErrorCode.INVALID_REVIEW_STAR, exception.getErrorCode());
  }

  @Test
  void 리뷰_추가_코멘트_비어있음_예외_발생() {
    // Given
    ReviewRequest request = ReviewRequest.builder()
        .accomodationId(1L)
        .comment("") // 비어 있는 후기 내용
        .star(5)
        .build();

    // When & Then
    GlobalException exception = assertThrows(GlobalException.class, () -> reviewService.addReview(1L, request));
    assertEquals(ReviewErrorCode.EMPTY_REVIEW_COMMENT, exception.getErrorCode());
  }
}

