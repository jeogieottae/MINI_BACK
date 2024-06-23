package com.example.mini.domain.reservation.service;

import com.example.mini.domain.accomodation.entity.Accomodation;
import com.example.mini.domain.accomodation.entity.Room;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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

  @InjectMocks
  private ReservationService reservationService;

  private Member member;
  private Room room;
  private Accomodation accomodation;
  private Reservation reservation;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.initMocks(this);

    member = Member.builder()
        .id(1L)
        .build();

    accomodation = Accomodation.builder()
        .id(1L)
        .name("Test Accomodation")
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
        .build();

    reservation = Reservation.builder()
        .id(1L)
        .room(room)
        .member(member)
        .checkIn(LocalDateTime.of(2023, 6, 20, 14, 0))
        .checkOut(LocalDateTime.of(2023, 6, 23, 11, 0))
        .status(ReservationStatus.PENDING)
        .peopleNumber(10)
        .build();
  }

  @Test
  void createConfirmedReservationValidRequestReturnsReservationResponse() {
    // Given
    Long memberId = 1L;
    ReservationRequest request = new ReservationRequest();
    request.setRoomId(room.getId());
    request.setPeopleNumber(2);
    request.setCheckIn(LocalDateTime.of(2024, 7, 1, 0, 0));
    request.setCheckOut(LocalDateTime.of(2024, 7, 5, 0, 0));

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
  }

  @Test
  void createConfirmedReservationRoomNotFoundThrowsGlobalException() {
    // Given
    Long memberId = 1L;
    ReservationRequest request = new ReservationRequest();
    request.setRoomId(999L); // Non-existent room ID

    when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
    when(roomRepository.findById(request.getRoomId())).thenReturn(Optional.empty());

    // When, Then
    GlobalException exception = assertThrows(GlobalException.class, () -> {
      reservationService.createConfirmedReservation(memberId, request);
    });
    assertEquals(ReservationErrorCode.ROOM_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  void getAllReservationsReturnsPageOfReservationSummaryResponse() {
    // Given
    Long memberId = 1L;
    Pageable pageable = Pageable.unpaged();
    Page<Reservation> page = new PageImpl<>(Collections.emptyList());

    when(reservationRepository.findReservationsByMemberId(memberId, ReservationStatus.CONFIRMED,
        pageable))
        .thenReturn(page);

    // When
    Page<ReservationSummaryResponse> result = reservationService.getAllReservations(memberId,
        pageable);

    // Then
    assertNotNull(result);
    assertTrue(result.getContent().isEmpty());
  }

  @Test
  void getReservationDetailValidReservationReturnsReservationDetailResponse() {
    // Given
    Long reservationId = 1L;
    Long memberId = 1L;

    when(reservationRepository.findByIdAndMemberId(reservationId, memberId))
        .thenReturn(Optional.of(reservation));

    // When
    ReservationDetailResponse response = reservationService.getReservationDetail(reservationId, memberId);

    // Then
    assertNotNull(response);
    assertEquals(reservation.getRoom().getPrice(), response.getRoomPrice());
    assertEquals(reservation.getRoom().getBaseGuests(), response.getBaseGuests());

    int totalGuests = reservation.getPeopleNumber();
    int baseGuests = reservation.getRoom().getBaseGuests();
    int extraPersonCharge = reservation.getRoom().getExtraPersonCharge();
    int expectedExtraCharge = totalGuests > baseGuests ? (totalGuests - baseGuests) * extraPersonCharge : 0;
    assertEquals(expectedExtraCharge, response.getExtraCharge());

    assertTrue(response.getParkingAvailable());
    assertTrue(response.getCookingAvailable());
  }
}
