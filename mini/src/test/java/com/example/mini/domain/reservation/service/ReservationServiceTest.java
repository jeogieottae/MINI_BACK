package com.example.mini.domain.reservation.service;

import com.example.mini.domain.accomodation.entity.Accomodation;
import com.example.mini.domain.accomodation.entity.Room;
import com.example.mini.domain.cart.model.request.ConfirmCartItemRequest;
import com.example.mini.domain.cart.model.response.CartConfirmResponse;
import com.example.mini.domain.member.entity.Member;
import com.example.mini.domain.reservation.entity.Reservation;
import com.example.mini.domain.reservation.entity.enums.ReservationStatus;
import com.example.mini.domain.reservation.model.request.ReservationRequest;
import com.example.mini.domain.reservation.model.response.ReservationDetailResponse;
import com.example.mini.domain.reservation.model.response.ReservationResponse;
import com.example.mini.domain.reservation.model.response.ReservationSummaryResponse;
import com.example.mini.domain.reservation.repository.ReservationRepository;
import com.example.mini.domain.accomodation.repository.RoomRepository;
import com.example.mini.domain.member.repository.MemberRepository;
import com.example.mini.global.api.exception.GlobalException;
import com.example.mini.global.api.exception.error.ReservationErrorCode;
import com.example.mini.global.email.EmailService;
import com.example.mini.global.model.dto.PagedResponse;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ReservationServiceTest {

  @Mock
  private ReservationRepository reservationRepository;

  @Mock
  private RoomRepository roomRepository;

  @Mock
  private MemberRepository memberRepository;
  @Mock
  private EmailService emailService;

  @InjectMocks
  private ReservationService reservationService;

  private Member member;
  private Room room;
  private Accomodation accomodation;
  private Reservation reservation;
  private Reservation existingReservation;
  private final int pageSize = 10;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.initMocks(this);

    member = Member.builder()
        .id(1L)
        .email("member@example.com")
        .build();

    accomodation = Accomodation.builder()
        .id(1L)
        .name("Test Accomodation")
        .address("123 Test St, Test City")
        .parkingAvailable(true)
        .cookingAvailable(true)
        .build();

    room = Room.builder()
        .id(1L)
        .name("Test Room")
        .price(100)
        .baseGuests(2)
        .maxGuests(4)
        .extraPersonCharge(10)
        .accomodation(accomodation)
        .images(new ArrayList<>())
        .build();

    reservation = Reservation.builder()
        .id(1L)
        .accomodation(accomodation)
        .room(room)
        .member(member)
        .checkIn(LocalDateTime.of(2023, 6, 20, 14, 0))
        .checkOut(LocalDateTime.of(2023, 6, 23, 11, 0))
        .status(ReservationStatus.PENDING)
        .peopleNumber(2)
        .build();

    existingReservation = Reservation.builder()
        .id(2L)
        .room(room)
        .member(member)
        .checkIn(LocalDateTime.of(2023, 6, 20, 14, 0))
        .checkOut(LocalDateTime.of(2023, 6, 23, 11, 0))
        .status(ReservationStatus.CONFIRMED)
        .peopleNumber(2)
        .build();
  }

  @Test
  void createConfirmedReservationValidRequestReturnsReservationResponse() { //예약 생성
    // Given
    Long memberId = 1L;
    ReservationRequest request = ReservationRequest.builder()
        .roomId(room.getId())
        .peopleNumber(2)
        .checkIn(LocalDateTime.of(2024, 7, 1, 0, 0))
        .checkOut(LocalDateTime.of(2024, 7, 5, 0, 0))
        .build();

    when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
    when(roomRepository.findById(request.getRoomId())).thenReturn(Optional.of(room));
    when(reservationRepository.findOverlappingReservations(anyList(), any(LocalDateTime.class),
        any(LocalDateTime.class)))
        .thenReturn(Collections.emptyList());

    // When
    ReservationResponse response = reservationService.createConfirmedReservation(memberId, request);

    // Then
    assertNotNull(response);
    assertEquals(request.getRoomId(), response.getRoomId());
    assertEquals(room.getAccomodation().getName(), response.getAccomodationName());
    assertEquals(room.getName(), response.getRoomName());
    assertEquals(room.getBaseGuests(), response.getBaseGuests());
    assertEquals(room.getMaxGuests(), response.getMaxGuests());
    assertEquals(request.getPeopleNumber(), response.getPeopleNumber());

    verify(emailService, times(1)).sendReservationConfirmationEmail(anyString(), anyString(), anyString());
  }

  @Test
  public void testConfirmReservationItemShouldSendConfirmationEmail() { //이메일 확인
    // Given
    ReservationRequest request = ReservationRequest.builder()
        .roomId(room.getId())
        .checkIn(reservation.getCheckIn())
        .checkOut(reservation.getCheckOut())
        .peopleNumber(reservation.getPeopleNumber())
        .build();

    when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
    when(roomRepository.findById(request.getRoomId())).thenReturn(Optional.of(room));
    when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));
    when(reservationRepository.findOverlappingReservations(List.of(room.getId()),
        reservation.getCheckIn(), reservation.getCheckOut())).thenReturn(new ArrayList<>());

    // When
    ReservationResponse response = reservationService.createConfirmedReservation(member.getId(), request);

    // Then
    ArgumentCaptor<String> toCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> textCaptor = ArgumentCaptor.forClass(String.class);

    verify(emailService).sendReservationConfirmationEmail(toCaptor.capture(), subjectCaptor.capture(), textCaptor.capture());

    assertEquals(member.getEmail(), toCaptor.getValue());
    assertEquals("예약 확정 되었습니다", subjectCaptor.getValue());
    assertTrue(textCaptor.getValue().contains("귀하의 Test Accomodation에서 Test Room 객실 예약이 확정되었습니다."));
    assertTrue(textCaptor.getValue().contains("체크인: 2023-06-20T14:00"));
    assertTrue(textCaptor.getValue().contains("체크아웃: 2023-06-23T11:00"));
    assertTrue(textCaptor.getValue().contains("인원 수: 2명"));
    assertTrue(textCaptor.getValue().contains("총 가격: 100원"));

    assertNotNull(response);
    assertEquals(room.getId(), response.getRoomId());
  }

  @Test
  void createConfirmedReservationInvalidMemberIdThrowsException() { //유효하지 않은 사용자 예외
    // Given
    Long invalidMemberId = 999L;
    ReservationRequest request = ReservationRequest.builder()
        .roomId(room.getId())
        .peopleNumber(2)
        .checkIn(LocalDateTime.of(2024, 7, 1, 0, 0))
        .checkOut(LocalDateTime.of(2024, 7, 5, 0, 0))
        .build();

    when(memberRepository.findById(invalidMemberId)).thenReturn(Optional.empty());

    // When / Then
    GlobalException exception = assertThrows(GlobalException.class, () -> reservationService.createConfirmedReservation(invalidMemberId, request));
    assertEquals(ReservationErrorCode.MEMBER_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  void createConfirmedReservationRoomNotFoundThrowsGlobalException() { //유효하지 않은 객실 예외
    // Given
    Long memberId = 1L;
    ReservationRequest request = ReservationRequest.builder()
        .roomId(999L).build();// Non-existent room ID

    when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
    when(roomRepository.findById(request.getRoomId())).thenReturn(Optional.empty());

    // When, Then
    GlobalException exception = assertThrows(GlobalException.class, () -> {
      reservationService.createConfirmedReservation(memberId, request);
    });
    assertEquals(ReservationErrorCode.ROOM_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  void createConfirmedReservationInvalidRequestThrowsException() { //인원 초과 예외
    // Given
    Long memberId = 1L;
    ReservationRequest request = ReservationRequest.builder()
        .roomId(room.getId())
        .peopleNumber(5) // Exceeding max guests
        .checkIn(LocalDateTime.of(2024, 7, 1, 0, 0))
        .checkOut(LocalDateTime.of(2024, 7, 5, 0, 0))
        .build();

    when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
    when(roomRepository.findById(request.getRoomId())).thenReturn(Optional.of(room));
    when(reservationRepository.findOverlappingReservations(anyList(), any(LocalDateTime.class),
        any(LocalDateTime.class)))
        .thenReturn(Collections.emptyList());

    // When / Then
    GlobalException exception = assertThrows(GlobalException.class, () -> reservationService.createConfirmedReservation(memberId, request));
    assertEquals(ReservationErrorCode.EXCEEDS_MAX_GUESTS, exception.getErrorCode());
  }

  @Test
  void validateReservationInvalidRequestWithConflictingReservationsThrowsException() { //중복예약 예외
    // Given
    Long memberId = 1L;
    ReservationRequest request = ReservationRequest.builder()
        .roomId(room.getId())
        .peopleNumber(2)
        .checkIn(LocalDateTime.of(2024, 7, 1, 0, 0))
        .checkOut(LocalDateTime.of(2024, 7, 5, 0, 0))
        .build();

    List<Reservation> conflictingReservations = Collections.singletonList(existingReservation);

    when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
    when(roomRepository.findById(request.getRoomId())).thenReturn(Optional.of(room));
    when(reservationRepository.findOverlappingReservationsByMemberId(eq(memberId), eq(request.getRoomId()),
        eq(request.getCheckIn()), eq(request.getCheckOut())))
        .thenReturn(conflictingReservations);

    // When / Then
    GlobalException exception = assertThrows(GlobalException.class, () -> reservationService.createConfirmedReservation(memberId, request));
    assertEquals(ReservationErrorCode.DUPLICATED_RESERVATION, exception.getErrorCode());
  }

  @Test
  void validateReservationWithConflictingReservationsThrowsException() { //예약 기간 예외
    // Given
    Long memberId = 1L;
    ReservationRequest request = ReservationRequest.builder()
        .roomId(room.getId())
        .peopleNumber(2)
        .checkIn(LocalDateTime.of(2024, 7, 1, 0, 0))
        .checkOut(LocalDateTime.of(2024, 7, 5, 0, 0))
        .build();

    List<Reservation> conflictingReservations = Collections.singletonList(existingReservation);

    when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
    when(roomRepository.findById(request.getRoomId())).thenReturn(Optional.of(room));
    when(reservationRepository.findOverlappingReservations(eq(List.of(request.getRoomId())), eq(request.getCheckIn()), eq(request.getCheckOut())))
        .thenReturn(conflictingReservations);

    // When / Then
    GlobalException exception = assertThrows(GlobalException.class, () -> reservationService.createConfirmedReservation(memberId, request));
    assertEquals(ReservationErrorCode.CONFLICTING_RESERVATION, exception.getErrorCode());
  }

  @Test
  void createConfirmedReservationInvalidCheckoutDateThrowsException() { //유효하지 않은 기간 예외
    // Given
    Long memberId = 1L;
    ReservationRequest request = ReservationRequest.builder()
        .roomId(room.getId())
        .peopleNumber(2)
        .checkIn(LocalDateTime.of(2024, 7, 5, 0, 0))
        .checkOut(LocalDateTime.of(2024, 7, 1, 0, 0))
        .build();

    when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
    when(roomRepository.findById(request.getRoomId())).thenReturn(Optional.of(room));

    // When / Then
    GlobalException exception = assertThrows(GlobalException.class, () -> reservationService.createConfirmedReservation(memberId, request));
    assertEquals(ReservationErrorCode.INVALID_CHECKOUT_DATE, exception.getErrorCode());
  }


  @Test
  void getAllReservationsReturnsPagedResponse() { //예약 전체조회
    // Given
    Long memberId = 1L;
    int page = 1;

    Page<Reservation> reservationsPage = new PageImpl<>(List.of(reservation), PageRequest.of(page - 1, pageSize), 1);

    when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
    when(reservationRepository.findReservationsByMemberId(eq(memberId), eq(ReservationStatus.CONFIRMED), eq(PageRequest.of(page - 1, pageSize))))
        .thenReturn(reservationsPage);

    // When
    PagedResponse<ReservationSummaryResponse> response = reservationService.getAllReservations(memberId, page);

    // Then
    assertNotNull(response);
    assertEquals(1, response.getTotalPages());
    assertEquals(1, response.getTotalElements());
    assertEquals(1, response.getContent().size());

    ReservationSummaryResponse summary = response.getContent().get(0);
    assertEquals(reservation.getId(), summary.getReservationId());
    assertEquals(reservation.getRoom().getAccomodation().getName(), summary.getAccomodationName());
    assertEquals(reservation.getRoom().getAccomodation().getAddress(), summary.getAccomodationAddress());
    assertEquals(reservation.getRoom().getName(), summary.getRoomName());
    assertEquals(reservation.getTotalPrice(), summary.getTotalPrice());
    assertEquals(reservation.getPeopleNumber(), summary.getPeopleNumber());
    assertEquals(reservation.getCheckIn(), summary.getCheckIn());
    assertEquals(reservation.getCheckOut(), summary.getCheckOut());
  }

  @Test
  void getAllReservationsThrowsExceptionForNonExistentMember() { //유효하지 않은 사용자 예외
    // Given
    Long memberId = 1L;
    int page = 1;

    when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

    // When / Then
    GlobalException exception = assertThrows(GlobalException.class, () -> reservationService.getAllReservations(memberId, page));
    assertEquals(ReservationErrorCode.MEMBER_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  void getReservationDetailReturnsDetailResponse() { //예약 상세조회
    // Given
    Long reservationId = 2L;
    Long memberId = 1L;

    when(reservationRepository.findByIdAndMemberId(eq(reservationId), eq(memberId)))
        .thenReturn(Optional.of(reservation));

    // When
    ReservationDetailResponse response = reservationService.getReservationDetail(reservationId, memberId);

    // Then
    assertNotNull(response);
    assertEquals(reservation.getMember().getName(), response.getMemberName());
    assertEquals(reservation.getRoom().getAccomodation().getName(), response.getAccomodationName());
    assertEquals(reservation.getRoom().getName(), response.getRoomName());
    assertEquals(reservation.getRoom().getPrice(), response.getRoomPrice());
    assertEquals(reservation.getRoom().getBaseGuests(), response.getBaseGuests());
    assertEquals(reservation.getExtraCharge(), response.getExtraCharge());
    assertEquals(reservation.getCheckIn(), response.getCheckIn());
    assertEquals(reservation.getCheckOut(), response.getCheckOut());
    assertEquals(reservation.getRoom().getAccomodation().getParkingAvailable(), response.getParkingAvailable());
    assertEquals(reservation.getRoom().getAccomodation().getCookingAvailable(), response.getCookingAvailable());
  }

  @Test
  void getReservationDetailThrowsExceptionForNonExistentReservation() { //유효하지않은 예약 예외
    // Given
    Long reservationId = 2L;
    Long memberId = 1L;

    when(reservationRepository.findByIdAndMemberId(eq(reservationId), eq(memberId)))
        .thenReturn(Optional.empty());

    // When / Then
    GlobalException exception = assertThrows(GlobalException.class, () -> reservationService.getReservationDetail(reservationId, memberId));
    assertEquals(ReservationErrorCode.RESERVATION_NOT_FOUND, exception.getErrorCode());
  }
}