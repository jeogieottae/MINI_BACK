package com.example.mini.domain.review.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import com.example.mini.domain.review.model.request.ReviewRequestDto;
import com.example.mini.domain.review.model.response.AccomodationReviewResponseDto;
import com.example.mini.domain.review.model.response.ReviewResponseDto;
import com.example.mini.domain.review.repository.ReviewRepository;
import com.example.mini.global.api.exception.GlobalException;
import com.example.mini.global.api.exception.error.ReviewErrorCode;
import com.example.mini.global.model.dto.PagedResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

class ReviewServiceTest { /*모두 성공*/

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
    ReviewRequestDto request = ReviewRequestDto.builder()
        .accomodationId(1L)
        .comment("좋았습니다!")
        .star(5)
        .build();

    when(reviewRepository.existsByReservation(reservation)).thenReturn(false);

    // When
    ReviewResponseDto response = reviewService.addReview(1L, request);

    // Then
    verify(reviewRepository, times(1)).save(any(Review.class));
    assertEquals(request.getComment(), response.getComment());
    assertEquals(request.getStar(), response.getStar());
  }

  @Test
  void 리뷰_추가_회원_없음_예외_발생() {
    // Given
    ReviewRequestDto request = ReviewRequestDto.builder()
        .accomodationId(1L)
        .comment("좋았습니다!")
        .star(5)
        .build();

    when(memberRepository.findById(1L)).thenReturn(java.util.Optional.empty());

    // When & Then
    GlobalException exception = assertThrows(GlobalException.class, () -> reviewService.addReview(1L, request));
    assertEquals(ReviewErrorCode.MEMBER_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  void 리뷰_추가_숙소_없음_예외_발생() {
    // Given
    ReviewRequestDto request = ReviewRequestDto.builder()
        .accomodationId(1L)
        .comment("좋았습니다!")
        .star(5)
        .build();

    when(accomodationRepository.findById(1L)).thenReturn(java.util.Optional.empty());

    // When & Then
    GlobalException exception = assertThrows(GlobalException.class, () -> reviewService.addReview(1L, request));
    assertEquals(ReviewErrorCode.ACCOMODATION_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  void 리뷰_추가_예약_없음_예외_발생() {
    // Given
    ReviewRequestDto request = ReviewRequestDto.builder()
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
    ReviewRequestDto request = ReviewRequestDto.builder()
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
    ReviewRequestDto request = ReviewRequestDto.builder()
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
    ReviewRequestDto request = ReviewRequestDto.builder()
        .accomodationId(1L)
        .comment("") // 비어 있는 후기 내용
        .star(5)
        .build();

    // When & Then
    GlobalException exception = assertThrows(GlobalException.class, () -> reviewService.addReview(1L, request));
    assertEquals(ReviewErrorCode.EMPTY_REVIEW_COMMENT, exception.getErrorCode());
  }

  @Test
  void 리뷰_추가_유효하지_않은_리뷰_작성_기간_예외_발생() {
    // Given
    ReviewRequestDto request = ReviewRequestDto.builder()
        .accomodationId(1L)
        .comment("좋았습니다!")
        .star(5)
        .build();

    // Fixture를 사용하여 예약 객체 생성
    Reservation originalReservation = ReservationEntityFixture.getReservation(member, accomodation, room);

    // 생성된 예약 객체를 빌더 패턴을 사용하여 복사하고, 체크아웃 시간을 수정
    Reservation reservation = Reservation.builder()
        .id(originalReservation.getId())
        .peopleNumber(originalReservation.getPeopleNumber())
        .extraCharge(originalReservation.getExtraCharge())
        .totalPrice(originalReservation.getTotalPrice())
        .checkIn(originalReservation.getCheckIn())
        .checkOut(LocalDateTime.now().plusDays(1)) // 체크아웃 시간을 현재 시점보다 1일 후로 설정
        .accomodation(originalReservation.getAccomodation())
        .member(originalReservation.getMember())
        .room(originalReservation.getRoom())
        .status(originalReservation.getStatus())
        .build();

    when(reservationRepository.findByMemberIdAndAccomodationIdAndStatus(1L, 1L, ReservationStatus.CONFIRMED)).thenReturn(java.util.Optional.of(reservation));

    // When & Then
    GlobalException exception = assertThrows(GlobalException.class, () -> reviewService.addReview(1L, request));
    assertEquals(ReviewErrorCode.INVALID_REVIEW_DATE, exception.getErrorCode());
  }

  @Test
  void 리뷰_조회_숙소_없음_예외_발생() {
    // Given
    Long accomodationId = 1L;
    int page = 1;

    when(accomodationRepository.findById(accomodationId)).thenReturn(Optional.empty());

    // When & Then
    GlobalException exception = assertThrows(GlobalException.class, () -> reviewService.getReviewsByAccomodationId(accomodationId, page));
    assertEquals(ReviewErrorCode.ACCOMODATION_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  void 리뷰_조회_정상() {
    // Given
    Long accomodationId = 1L;
    int page = 1;
    Review review1 = Review.builder()
        .comment("좋았습니다!")
        .star(5)
        .member(member)
        .accomodation(accomodation)
        .reservation(reservation)
        .build();

    Review review2 = Review.builder()
        .comment("별로였습니다!")
        .star(2)
        .member(member)
        .accomodation(accomodation)
        .reservation(reservation)
        .build();

    Page<Review> reviewPage = new PageImpl<>(List.of(review1, review2), PageRequest.of(page - 1, 10), 2);

    when(accomodationRepository.findById(accomodationId)).thenReturn(Optional.of(accomodation));
    when(reviewRepository.findByAccomodationOrderByCreatedAtDesc(accomodation, PageRequest.of(page - 1, 10))).thenReturn(reviewPage);

    // When
    PagedResponse<AccomodationReviewResponseDto> response = reviewService.getReviewsByAccomodationId(accomodationId, page);

    // Then
    assertEquals(1, response.getTotalPages());
    assertEquals(2, response.getTotalElements());
    assertEquals(2, response.getContent().size());
  }

  @Test
  void 리뷰_조회_빈_결과() {
    // Given
    Long accomodationId = 1L;
    int page = 1;
    Page<Review> reviewPage = new PageImpl<>(List.of(), PageRequest.of(page - 1, 10), 0);

    when(accomodationRepository.findById(accomodationId)).thenReturn(Optional.of(accomodation));
    when(reviewRepository.findByAccomodationOrderByCreatedAtDesc(accomodation, PageRequest.of(page - 1, 10))).thenReturn(reviewPage);

    // When
    PagedResponse<AccomodationReviewResponseDto> response = reviewService.getReviewsByAccomodationId(accomodationId, page);

    // Then
    assertEquals(0, response.getTotalPages());
    assertEquals(0, response.getTotalElements());
    assertTrue(response.getContent().isEmpty());
  }
}
