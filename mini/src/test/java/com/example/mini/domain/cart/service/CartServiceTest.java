package com.example.mini.domain.cart.service;

import com.example.mini.domain.accomodation.entity.Accomodation;
import com.example.mini.domain.accomodation.entity.Room;
import com.example.mini.domain.cart.entity.Cart;
import com.example.mini.domain.cart.model.request.AddCartItemRequest;
import com.example.mini.domain.cart.model.request.ConfirmCartItemRequest;
import com.example.mini.domain.cart.model.request.DeleteCartItemRequest;
import com.example.mini.domain.cart.model.response.CartResponse;
import com.example.mini.domain.cart.repository.CartRepository;
import com.example.mini.domain.member.entity.Member;
import com.example.mini.domain.member.repository.MemberRepository;
import com.example.mini.domain.accomodation.repository.RoomRepository;
import com.example.mini.domain.reservation.entity.Reservation;
import com.example.mini.domain.reservation.entity.enums.ReservationStatus;
import com.example.mini.domain.reservation.repository.ReservationRepository;
import com.example.mini.global.api.exception.GlobalException;
import com.example.mini.global.api.exception.error.CartErrorCode;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CartServiceTest {

  @Mock
  private MemberRepository memberRepository;

  @Mock
  private CartRepository cartRepository;

  @Mock
  private ReservationRepository reservationRepository;

  @Mock
  private RoomRepository roomRepository;

  @InjectMocks
  private CartService cartService;

  private Member member;
  private Cart cart;
  private Room room;
  private Reservation reservation;
  private Accomodation accomodation;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);

    member = Member.builder()
        .id(1L)
        .build();

    accomodation = Accomodation.builder()
        .id(1L)
        .name("Test Accomodation")
        .build();

    room = Room.builder()
        .id(1L)
        .price(100)
        .baseGuests(2)
        .maxGuests(4)
        .extraPersonCharge(20)
        .accomodation(accomodation)
        .build();

    reservation = Reservation.builder()
        .id(1L)
        .room(room)
        .member(member)
        .checkIn(LocalDateTime.of(2023, 6, 20, 14, 0))
        .checkOut(LocalDateTime.of(2023, 6, 23, 11, 0))
        .status(ReservationStatus.PENDING)
        .build();

    cart = Cart.builder()
        .member(member)
        .reservationList(new ArrayList<>())
        .build();
    cart.getReservationList().add(reservation);
  }

  @Test
  public void testGetAllCartItemsShouldReturnCartItems() {
    // Given
    Pageable pageable = PageRequest.of(0, 10);
    Page<Reservation> reservations = new PageImpl<>(List.of(reservation), pageable, 1);

    when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
    when(cartRepository.findByMember(member)).thenReturn(Optional.of(cart));
    when(reservationRepository.findReservationsByMemberId(member.getId(), ReservationStatus.PENDING,
        pageable)).thenReturn(reservations);

    // When
    Page<CartResponse> result = cartService.getAllCartItems(member.getId(), pageable);

    // Then
    assertEquals(1, result.getTotalElements());
    verify(memberRepository).findById(member.getId());
    verify(cartRepository).findByMember(member);
    verify(reservationRepository).findReservationsByMemberId(member.getId(),
        ReservationStatus.PENDING, pageable);
  }

  @Test
  public void testAddCartItemWithValidRequestShouldAddCartItem() {
    // Given
    AddCartItemRequest request = AddCartItemRequest.builder()
        .roomId(room.getId())
        .checkIn(LocalDateTime.of(2023, 6, 21, 15, 0))
        .checkOut(LocalDateTime.of(2023, 6, 25, 12, 0))
        .peopleNumber(2)
        .build();

    when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
    when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));
    when(cartRepository.findByMember(member)).thenReturn(Optional.of(cart));
    when(reservationRepository.findOverlappingReservations(List.of(room.getId()),
        request.getCheckIn(), request.getCheckOut())).thenReturn(new ArrayList<>());

    // When
    cartService.addCartItem(member.getId(), request);

    // Then
    verify(cartRepository).save(cart);
  }

  @Test
  public void testDeleteCartItemWithValidRequestShouldDeleteCartItem() {
    // Given
    DeleteCartItemRequest request = DeleteCartItemRequest.builder()
        .reservationIds(List.of(reservation.getId()))
        .build();

    when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
    when(cartRepository.findByMember(member)).thenReturn(Optional.of(cart));
    when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));

    // When
    cartService.deleteCartItem(member.getId(), request);

    // Then
    verify(cartRepository).save(cart);
  }

  @Test
  public void confirmReservationItemInvalidCheckOutDateShouldThrowGlobalException() {
    // Given
    Long memberId = 1L;
    Long reservationId = 10L;
    Long roomId = 20L;
    int peopleNumber = 2;
    LocalDateTime checkIn = LocalDateTime.now().plusDays(1);
    LocalDateTime checkOut = LocalDateTime.now().minusDays(1);

    ConfirmCartItemRequest request = new ConfirmCartItemRequest(
        reservationId, roomId, peopleNumber, checkIn, checkOut
    );

    when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
    when(cartRepository.findByMember(member)).thenReturn(Optional.of(cart));
    when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

    // When / Then
    GlobalException exception = assertThrows(GlobalException.class,
        () -> cartService.confirmReservationItem(memberId, request));

    assertEquals(CartErrorCode.INVALID_CHECKOUT_DATE, exception.getErrorCode());
    verify(reservationRepository, never()).updateReservationDetails(anyInt(), any(LocalDateTime.class), any(LocalDateTime.class), anyLong());
    verify(reservationRepository, never()).updateReservationStatus(anyLong(), any(ReservationStatus.class));
  }
}