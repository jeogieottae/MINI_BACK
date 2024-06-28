package com.example.mini.domain.review.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.mini.domain.accomodation.entity.Accomodation;
import com.example.mini.domain.accomodation.repository.AccomodationRepository;
import com.example.mini.domain.member.entity.Member;
import com.example.mini.domain.member.repository.MemberRepository;
import com.example.mini.domain.reservation.entity.Reservation;
import com.example.mini.domain.reservation.entity.enums.ReservationStatus;
import com.example.mini.domain.reservation.repository.ReservationRepository;
import com.example.mini.domain.review.entity.Review;
import com.example.mini.domain.review.model.request.ReviewRequest;
import com.example.mini.domain.review.model.response.ReviewResponse;
import com.example.mini.domain.review.repository.ReviewRepository;
import com.example.mini.global.api.exception.GlobalException;
import java.time.LocalDateTime;
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

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void addReview_GivenValidRequest_WhenAllConditionsMet_ThenReviewIsAdded() {
    // Given
    Long memberId = 1L;
    ReviewRequest request = ReviewRequest.builder()
            .accomodationId(1L)
            .comment("Great stay!")
            .star(5)
            .build();

    Member member = new Member();
    member.setId(memberId);

    Accomodation accomodation = new Accomodation();
    accomodation.setId(1L);

    Reservation reservation = Reservation.builder()
        .status(ReservationStatus.CONFIRMED)
        .checkOut(LocalDateTime.now().minusDays(1))
        .build();

    when(memberRepository.findById(memberId)).thenReturn(java.util.Optional.of(member));
    when(accomodationRepository.findById(request.getAccomodationId())).thenReturn(java.util.Optional.of(accomodation));
    when(reservationRepository.findByMemberIdAndAccomodationIdAndStatus(memberId, request.getAccomodationId(), ReservationStatus.CONFIRMED)).thenReturn(java.util.Optional.of(reservation));
    when(reviewRepository.existsByReservation(reservation)).thenReturn(false);

    // When
    ReviewResponse response = reviewService.addReview(memberId, request);

    // Then
    verify(reviewRepository, times(1)).save(any(Review.class));
    assert response.getComment().equals(request.getComment());
    assert response.getStar().equals(request.getStar());
  }

  @Test
  void addReview_GivenInvalidRequest_WhenReservationNotFound_ThenThrowsException() {
    // Given
    Long memberId = 1L;
    ReviewRequest request = ReviewRequest.builder()
            .accomodationId(1L)
            .comment("Great stay!")
            .star(5)
            .build();

    Member member = new Member();
    member.setId(memberId);

    when(memberRepository.findById(memberId)).thenReturn(java.util.Optional.of(member));
    when(accomodationRepository.findById(request.getAccomodationId())).thenReturn(java.util.Optional.of(new Accomodation()));
    when(reservationRepository.findByMemberIdAndAccomodationIdAndStatus(memberId, request.getAccomodationId(), ReservationStatus.CONFIRMED)).thenReturn(java.util.Optional.empty());

    // When & Then
    assertThrows(GlobalException.class, () -> reviewService.addReview(memberId, request));
  }

  @Test
  void addReview_GivenInvalidRequest_WhenReviewExists_ThenThrowsException() {
    // Given
    Long memberId = 1L;
    ReviewRequest request = ReviewRequest.builder()
            .accomodationId(1L)
            .comment("Great stay!")
            .star(5)
            .build();

    Member member = new Member();
    member.setId(memberId);

    Accomodation accomodation = new Accomodation();
    accomodation.setId(1L);

    Reservation reservation = Reservation.builder()
        .status(ReservationStatus.CONFIRMED)
        .checkOut(LocalDateTime.now().minusDays(1))
        .build();

    when(memberRepository.findById(memberId)).thenReturn(java.util.Optional.of(member));
    when(accomodationRepository.findById(request.getAccomodationId())).thenReturn(java.util.Optional.of(accomodation));
    when(reservationRepository.findByMemberIdAndAccomodationIdAndStatus(memberId, request.getAccomodationId(), ReservationStatus.CONFIRMED)).thenReturn(java.util.Optional.of(reservation));
    when(reviewRepository.existsByReservation(reservation)).thenReturn(true);

    // When & Then
    assertThrows(GlobalException.class, () -> reviewService.addReview(memberId, request));
  }

  @Test
  void addReview_GivenInvalidRequest_WhenInvalidStar_ThenThrowsException() {
    // Given
    Long memberId = 1L;
    ReviewRequest request = ReviewRequest.builder()
            .accomodationId(1L)
            .comment("Great stay!")
            .star(5)
            .build();

    // When & Then
    assertThrows(GlobalException.class, () -> reviewService.addReview(memberId, request));
  }

  @Test
  void addReview_GivenInvalidRequest_WhenEmptyComment_ThenThrowsException() {
    // Given
    Long memberId = 1L;
    ReviewRequest request = ReviewRequest.builder()
            .accomodationId(1L)
            .comment("Great stay!")
            .star(5)
            .build();

    // When & Then
    assertThrows(GlobalException.class, () -> reviewService.addReview(memberId, request));
  }
}
