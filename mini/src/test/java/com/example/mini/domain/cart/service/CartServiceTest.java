package com.example.mini.domain.cart.service;

import com.example.mini.domain.accomodation.entity.Accomodation;
import com.example.mini.domain.accomodation.entity.Room;
import com.example.mini.domain.cart.entity.Cart;
import com.example.mini.domain.cart.model.request.AddCartItemRequest;
import com.example.mini.domain.cart.model.request.ConfirmCartItemRequest;
import com.example.mini.domain.cart.model.request.ConfirmCartItemRequest.ConfirmItem;
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
    AddCartItemRequest request = new AddCartItemRequest();
    request.setRoomId(room.getId());
    request.setCheckIn(LocalDateTime.of(2023, 6, 21, 15, 0));
    request.setCheckOut(LocalDateTime.of(2023, 6, 25, 12, 0));
    request.setPeopleNumber(2);

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
    DeleteCartItemRequest request = new DeleteCartItemRequest();
    request.setReservationIds(List.of(reservation.getId()));

    when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
    when(cartRepository.findByMember(member)).thenReturn(Optional.of(cart));
    when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));

    // When
    cartService.deleteCartItem(member.getId(), request);

    // Then
    verify(cartRepository).save(cart);
  }

  @Test
  public void testConfirmCartItemsWithInvalidDatesShouldThrowException() {
    // Given
    ConfirmCartItemRequest request = new ConfirmCartItemRequest();
    ConfirmItem confirmItem = new ConfirmItem();
    confirmItem.setReservationId(reservation.getId());
    confirmItem.setRoomId(room.getId());
    confirmItem.setCheckIn(reservation.getCheckIn());
    confirmItem.setCheckOut(LocalDateTime.of(2023, 6, 19, 11, 0));
    request.setConfirmItems(List.of(confirmItem));

    // When & Then
    assertThrows(GlobalException.class, () -> {
      cartService.confirmCartItems(member.getId(), request);
    });
  }

  @Test
  public void testConfirmCartItemsWithReservationMismatchShouldThrowException() {
    // Given
    ConfirmCartItemRequest request = new ConfirmCartItemRequest();
    ConfirmItem confirmItem = new ConfirmItem();
    confirmItem.setReservationId(reservation.getId());
    confirmItem.setRoomId(2L);
    confirmItem.setCheckIn(reservation.getCheckIn());
    confirmItem.setCheckOut(reservation.getCheckOut());
    request.setConfirmItems(List.of(confirmItem));

    when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
    when(cartRepository.findByMember(member)).thenReturn(Optional.of(cart));
    when(reservationRepository.findById(request.getConfirmItems().get(0).getReservationId())).thenReturn(Optional.of(reservation));

    // When & Then
    assertThrows(GlobalException.class, () -> {
      cartService.confirmCartItems(member.getId(), request);
    });
  }

  @Test
  public void testConfirmCartItemsWithValidRequestShouldConfirmReservation() {
    // Given
    ConfirmCartItemRequest request = new ConfirmCartItemRequest();
    ConfirmItem confirmItem = new ConfirmItem();
    confirmItem.setReservationId(reservation.getId());
    confirmItem.setRoomId(room.getId());
    confirmItem.setCheckIn(reservation.getCheckIn());
    confirmItem.setCheckOut(reservation.getCheckOut());
    request.setConfirmItems(List.of(confirmItem));

    when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
    when(cartRepository.findByMember(member)).thenReturn(Optional.of(cart));
    when(reservationRepository.findById(request.getConfirmItems().get(0).getReservationId())).thenReturn(Optional.of(reservation));

    // When
    cartService.confirmCartItems(member.getId(), request);

    // Then
    verify(reservationRepository).updateReservationStatus(request.getConfirmItems().get(0).getReservationId(), ReservationStatus.CONFIRMED);
  }
}