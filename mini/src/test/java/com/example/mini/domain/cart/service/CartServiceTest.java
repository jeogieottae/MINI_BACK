package com.example.mini.domain.cart.service;

import com.example.mini.domain.accomodation.entity.Accomodation;
import com.example.mini.domain.accomodation.entity.Room;
import com.example.mini.domain.cart.entity.Cart;
import com.example.mini.domain.cart.model.request.AddCartItemRequest;
import com.example.mini.domain.cart.model.request.ConfirmCartItemRequest;
import com.example.mini.domain.cart.model.request.DeleteCartItemRequest;
import com.example.mini.domain.cart.model.response.CartConfirmResponse;
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
import com.example.mini.global.email.EmailService;

import com.example.mini.global.model.dto.PagedResponse;
import java.util.Arrays;
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

  @Mock
  private EmailService emailService;

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
        .email("test@example.com")
        .build();

    accomodation = Accomodation.builder()
        .id(1L)
        .name("Test Accomodation")
        .build();

    room = Room.builder()
        .id(1L)
        .name("Test Room")
        .price(100)
        .baseGuests(2)
        .maxGuests(4)
        .extraPersonCharge(20)
        .accomodation(accomodation)
        .images(new ArrayList<>())
        .build();

    reservation = Reservation.builder()
        .id(1L)
        .room(room)
        .member(member)
        .accomodation(accomodation)
        .checkIn(LocalDateTime.of(2023, 6, 20, 14, 0))
        .checkOut(LocalDateTime.of(2023, 6, 23, 11, 0))
        .peopleNumber(2)
        .totalPrice(100)
        .status(ReservationStatus.PENDING)
        .build();

    cart = Cart.builder()
        .id(1L)
        .member(member)
        .reservationList(new ArrayList<>())
        .build();
    cart.getReservationList().add(reservation);
  }

  @Test
  public void testGetAllCartItemsShouldReturnCartItems() { //전체 장바구니항목 조회
    // Given
    Pageable pageable = PageRequest.of(0, 10);
    Page<Reservation> reservationsPage = new PageImpl<>(List.of(reservation), pageable, 1);

    when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
    when(cartRepository.findByMember(member)).thenReturn(Optional.of(cart));
    when(reservationRepository.findReservationsByMemberId(member.getId(), ReservationStatus.PENDING, pageable))
        .thenReturn(reservationsPage);

    // When
    PagedResponse<CartResponse> result = cartService.getAllCartItems(member.getId(), 1);

    // Then
    assertEquals(1, result.getTotalElements());
    verify(memberRepository).findById(member.getId());
    verify(cartRepository).findByMember(member);
    verify(reservationRepository).findReservationsByMemberId(member.getId(), ReservationStatus.PENDING, pageable);
  }

  @Test
  public void testGetAllCartItemsShouldThrowExceptionWhenMemberNotFound() { //사용자 정보 예외
    // Given
    when(memberRepository.findById(member.getId())).thenReturn(Optional.empty());

    // When / Then
    GlobalException exception = assertThrows(GlobalException.class,
        () -> cartService.getAllCartItems(member.getId(), 1));

    assertEquals(CartErrorCode.MEMBER_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  public void testGetAllCartItemsShouldThrowExceptionWhenCartNotFound() { //장바구니 정보 예외
    // Given
    when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
    when(cartRepository.findByMember(member)).thenReturn(Optional.empty());

    // When / Then
    GlobalException exception = assertThrows(GlobalException.class,
        () -> cartService.getAllCartItems(member.getId(), 1));

    assertEquals(CartErrorCode.CART_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  public void testAddCartItemWithValidRequestShouldAddCartItem() { //장바구니 항목 추가
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
  public void testAddCartItemShouldThrowExceptionWhenRoomNotFound() { //객실 정보 예외
    // Given
    AddCartItemRequest request = AddCartItemRequest.builder()
        .roomId(room.getId())
        .checkIn(LocalDateTime.of(2023, 6, 21, 15, 0))
        .checkOut(LocalDateTime.of(2023, 6, 25, 12, 0))
        .peopleNumber(2)
        .build();

    when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
    when(roomRepository.findById(room.getId())).thenReturn(Optional.empty());

    // When / Then
    GlobalException exception = assertThrows(GlobalException.class,
        () -> cartService.addCartItem(member.getId(), request));

    assertEquals(CartErrorCode.ROOM_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  public void testAddCartItemShouldThrowExceptionWhenExceedsMaxGuests() { //인원 초과 예외
    // Given
    AddCartItemRequest request = AddCartItemRequest.builder()
        .roomId(room.getId())
        .checkIn(LocalDateTime.of(2023, 6, 21, 15, 0))
        .checkOut(LocalDateTime.of(2023, 6, 25, 12, 0))
        .peopleNumber(5)
        .build();

    when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
    when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));

    // When / Then
    GlobalException exception = assertThrows(GlobalException.class,
        () -> cartService.addCartItem(member.getId(), request));

    assertEquals(CartErrorCode.EXCEEDS_MAX_GUESTS, exception.getErrorCode());
  }

  @Test
  public void testAddCartItemShouldThrowExceptionWhenInvalidCheckoutDate() { //유효하지않은 기간 예외
    // Given
    AddCartItemRequest request = AddCartItemRequest.builder()
        .roomId(room.getId())
        .checkIn(LocalDateTime.of(2023, 6, 25, 15, 0))
        .checkOut(LocalDateTime.of(2023, 6, 21, 12, 0))
        .peopleNumber(2)
        .build();

    when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
    when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));

    // When / Then
    GlobalException exception = assertThrows(GlobalException.class,
        () -> cartService.addCartItem(member.getId(), request));

    assertEquals(CartErrorCode.INVALID_CHECKOUT_DATE, exception.getErrorCode());
  }

  @Test
  public void testAddCartItemShouldThrowExceptionWhenConflictingReservation() { //이미 존재하는 항목 예외
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

    Reservation overlappingReservation = Reservation.builder()
        .id(2L)
        .room(room)
        .member(member)
        .checkIn(LocalDateTime.of(2023, 6, 22, 14, 0)) // 겹치는 기간
        .checkOut(LocalDateTime.of(2023, 6, 24, 11, 0)) // 겹치는 기간
        .peopleNumber(3)
        .status(ReservationStatus.CONFIRMED)
        .build();

    when(reservationRepository.findOverlappingReservations(
        eq(List.of(room.getId())),
        eq(request.getCheckIn()),
        eq(request.getCheckOut()))).thenReturn(List.of(overlappingReservation));

    // When / Then
    GlobalException exception = assertThrows(GlobalException.class,
        () -> cartService.addCartItem(member.getId(), request));

    assertEquals(CartErrorCode.CONFLICTING_RESERVATION, exception.getErrorCode());
  }


  @Test
  public void testAddCartItemShouldThrowExceptionWhenDuplicateReservation() { //이미 존재하는 항목 예외
    // Given
    AddCartItemRequest request = AddCartItemRequest.builder()
        .roomId(room.getId())
        .checkIn(LocalDateTime.of(2023, 6, 20, 14, 0))
        .checkOut(LocalDateTime.of(2023, 6, 23, 11, 0))
        .peopleNumber(2)
        .build();

    when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
    when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));
    when(cartRepository.findByMember(member)).thenReturn(Optional.of(cart));

    // When / Then
    GlobalException exception = assertThrows(GlobalException.class,
        () -> cartService.addCartItem(member.getId(), request));

    assertEquals(CartErrorCode.DUPLICATE_RESERVATION, exception.getErrorCode());
  }



  @Test
  public void testDeleteCartItemWithValidRequestShouldDeleteCartItem() { //장바구니 항목 삭제
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
  public void testDeleteCartItemShouldThrowExceptionWhenCartNotFound() { //장바구니 정보 예외
    // Given
    DeleteCartItemRequest request = DeleteCartItemRequest.builder()
        .reservationIds(List.of(reservation.getId()))
        .build();

    when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
    when(cartRepository.findByMember(member)).thenReturn(Optional.empty());

    // When / Then
    GlobalException exception = assertThrows(GlobalException.class,
        () -> cartService.deleteCartItem(member.getId(), request));

    assertEquals(CartErrorCode.CART_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  public void testDeleteCartItemShouldThrowExceptionWhenReservationNotFound() { //장바구니 항목 예외
    // Given
    DeleteCartItemRequest request = DeleteCartItemRequest.builder()
        .reservationIds(List.of(2L))
        .build();

    when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
    when(cartRepository.findByMember(member)).thenReturn(Optional.of(cart));
    when(reservationRepository.findById(2L)).thenReturn(Optional.empty());

    // When / Then
    GlobalException exception = assertThrows(GlobalException.class,
        () -> cartService.deleteCartItem(member.getId(), request));

    assertEquals(CartErrorCode.RESERVATION_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  public void testDeleteCartItemShouldThrowExceptionWhenReservationNotInCart() { //잘못된 예약 정보 예외
    // Given
    Reservation otherReservation = Reservation.builder()
        .id(2L)
        .room(room)
        .member(member)
        .checkIn(LocalDateTime.of(2023, 7, 1, 14, 0))
        .checkOut(LocalDateTime.of(2023, 7, 5, 11, 0))
        .status(ReservationStatus.PENDING)
        .build();

    DeleteCartItemRequest request = DeleteCartItemRequest.builder()
        .reservationIds(List.of(otherReservation.getId()))
        .build();

    when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
    when(cartRepository.findByMember(member)).thenReturn(Optional.of(cart));
    when(reservationRepository.findById(otherReservation.getId())).thenReturn(Optional.of(otherReservation));

    // When / Then
    GlobalException exception = assertThrows(GlobalException.class,
        () -> cartService.deleteCartItem(member.getId(), request));

    assertEquals(CartErrorCode.RESERVATION_NOT_IN_CART, exception.getErrorCode());
  }

  @Test
  public void testDeleteCartItemShouldThrowExceptionWhenReservationNotPending() { //예약 상태 예외
    // Given
    Reservation reservation = Reservation.builder()
        .status(ReservationStatus.CONFIRMED)
        .id(1L)
        .build();

    cart = Cart.builder()
        .id(1L)
        .member(member)
        .reservationList(new ArrayList<>())
        .build();
    cart.getReservationList().add(reservation);

    DeleteCartItemRequest request = DeleteCartItemRequest.builder()
        .reservationIds(Arrays.asList(reservation.getId()))
        .build();

    when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
    when(cartRepository.findByMember(member)).thenReturn(Optional.of(cart));
    when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));

    // When / Then
    GlobalException exception = assertThrows(GlobalException.class,
        () -> cartService.deleteCartItem(member.getId(), request));

    assertEquals(CartErrorCode.RESERVATION_NOT_PENDING, exception.getErrorCode());
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
    verify(reservationRepository, never()).updateReservationDetails(anyInt(), any(LocalDateTime.class), any(LocalDateTime.class), any(ReservationStatus.class), anyLong());
  }

  @Test
  public void testConfirmReservationItemShouldThrowExceptionWhenCartNotFound() { //예약확정 장바구니 예외
    // Given
    ConfirmCartItemRequest request = ConfirmCartItemRequest.builder()
        .reservationId(reservation.getId())
        .roomId(room.getId())
        .checkIn(reservation.getCheckIn())
        .checkOut(reservation.getCheckOut())
        .peopleNumber(reservation.getPeopleNumber())
        .build();

    when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
    when(cartRepository.findByMember(member)).thenReturn(Optional.empty());

    // When / Then
    GlobalException exception = assertThrows(GlobalException.class,
        () -> cartService.confirmReservationItem(member.getId(), request));

    assertEquals(CartErrorCode.CART_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  public void testConfirmReservationItemShouldThrowExceptionWhenReservationNotFound() { //예약정보 예외
    // Given
    ConfirmCartItemRequest request = ConfirmCartItemRequest.builder()
        .reservationId(2L)
        .roomId(room.getId())
        .checkIn(reservation.getCheckIn())
        .checkOut(reservation.getCheckOut())
        .peopleNumber(reservation.getPeopleNumber())
        .build();

    when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
    when(cartRepository.findByMember(member)).thenReturn(Optional.of(cart));
    when(reservationRepository.findById(2L)).thenReturn(Optional.empty());

    // When / Then
    GlobalException exception = assertThrows(GlobalException.class,
        () -> cartService.confirmReservationItem(member.getId(), request));

    assertEquals(CartErrorCode.RESERVATION_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  public void testConfirmReservationItemShouldThrowExceptionWhenReservationMismatch() { //잘못된 예약정보 예외
    // Given
    ConfirmCartItemRequest request = ConfirmCartItemRequest.builder()
        .reservationId(reservation.getId())
        .roomId(2L)
        .checkIn(reservation.getCheckIn())
        .checkOut(reservation.getCheckOut())
        .peopleNumber(reservation.getPeopleNumber())
        .build();

    when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
    when(cartRepository.findByMember(member)).thenReturn(Optional.of(cart));
    when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));

    // When / Then
    GlobalException exception = assertThrows(GlobalException.class,
        () -> cartService.confirmReservationItem(member.getId(), request));

    assertEquals(CartErrorCode.RESERVATION_MISMATCH, exception.getErrorCode());
  }

  @Test
  public void testConfirmReservationItemShouldThrowExceptionWhenReservationNotBelongsToUser() { //잘못된 예약정보 예외
    // Given
    Member otherMember = Member.builder()
        .id(2L)
        .build();

    Reservation otherReservation = Reservation.builder()
        .id(2L)
        .member(otherMember)
        .room(room)
        .checkIn(LocalDateTime.of(2023, 6, 24, 14, 0))
        .checkOut(LocalDateTime.of(2023, 6, 28, 11, 0))
        .peopleNumber(2)
        .status(ReservationStatus.PENDING)
        .build();

    ConfirmCartItemRequest request = ConfirmCartItemRequest.builder()
        .reservationId(otherReservation.getId())
        .roomId(room.getId())
        .checkIn(otherReservation.getCheckIn())
        .checkOut(otherReservation.getCheckOut())
        .peopleNumber(otherReservation.getPeopleNumber())
        .build();

    when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
    when(cartRepository.findByMember(member)).thenReturn(Optional.of(cart));
    when(reservationRepository.findById(otherReservation.getId())).thenReturn(Optional.of(otherReservation));

    // When / Then
    GlobalException exception = assertThrows(GlobalException.class,
        () -> cartService.confirmReservationItem(member.getId(), request));

    assertEquals(CartErrorCode.RESERVATION_NOT_BELONGS_TO_USER, exception.getErrorCode());
  }

  @Test
  public void testConfirmReservationItemShouldThrowExceptionWhenReservationNotInCart() { //잘못된 예약정보 예외
    // Given
    Reservation otherReservation = Reservation.builder()
        .id(2L)
        .room(room)
        .member(member)
        .accomodation(accomodation)
        .checkIn(LocalDateTime.of(2023, 6, 24, 14, 0))
        .checkOut(LocalDateTime.of(2023, 6, 28, 11, 0))
        .peopleNumber(2)
        .status(ReservationStatus.PENDING)
        .build();

    ConfirmCartItemRequest request = ConfirmCartItemRequest.builder()
        .reservationId(otherReservation.getId())
        .roomId(room.getId())
        .checkIn(otherReservation.getCheckIn())
        .checkOut(otherReservation.getCheckOut())
        .peopleNumber(otherReservation.getPeopleNumber())
        .build();

    when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
    when(cartRepository.findByMember(member)).thenReturn(Optional.of(cart));
    when(reservationRepository.findById(otherReservation.getId())).thenReturn(Optional.of(otherReservation));

    // When / Then
    GlobalException exception = assertThrows(GlobalException.class,
        () -> cartService.confirmReservationItem(member.getId(), request));

    assertEquals(CartErrorCode.RESERVATION_NOT_IN_CART, exception.getErrorCode());
  }

  @Test
  public void testConfirmReservationItemShouldThrowExceptionWhenConflictingReservationExists() { //겹치는 기간 예외
    // Given
    ConfirmCartItemRequest request = ConfirmCartItemRequest.builder()
        .reservationId(reservation.getId())
        .roomId(room.getId())
        .checkIn(reservation.getCheckIn())
        .checkOut(reservation.getCheckOut())
        .peopleNumber(reservation.getPeopleNumber())
        .build();

    Reservation conflictingReservation = Reservation.builder()
        .id(2L)
        .room(room)
        .checkIn(reservation.getCheckIn())
        .checkOut(reservation.getCheckOut())
        .status(ReservationStatus.CONFIRMED)
        .build();

    when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
    when(cartRepository.findByMember(member)).thenReturn(Optional.of(cart));
    when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));
    when(reservationRepository.findOverlappingReservations(List.of(room.getId()),
        reservation.getCheckIn(), reservation.getCheckOut())).thenReturn(List.of(conflictingReservation));

    // When / Then
    GlobalException exception = assertThrows(GlobalException.class,
        () -> cartService.confirmReservationItem(member.getId(), request));

    assertEquals(CartErrorCode.CONFLICTING_RESERVATION, exception.getErrorCode());
  }

  @Test
  public void testConfirmReservationItemShouldThrowExceptionWhenExceedsMaxGuests() { //예약 인원 초과
    // Given
    ConfirmCartItemRequest request = ConfirmCartItemRequest.builder()
        .reservationId(reservation.getId())
        .roomId(room.getId())
        .checkIn(reservation.getCheckIn())
        .checkOut(reservation.getCheckOut())
        .peopleNumber(5)
        .build();

    when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
    when(cartRepository.findByMember(member)).thenReturn(Optional.of(cart));
    when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));

    // When / Then
    GlobalException exception = assertThrows(GlobalException.class,
        () -> cartService.confirmReservationItem(member.getId(), request));

    assertEquals(CartErrorCode.EXCEEDS_MAX_GUESTS, exception.getErrorCode());
  }

  @Test
  public void testConfirmReservationItemShouldSendConfirmationEmail() { //이메일 확인
    // Given
    ConfirmCartItemRequest request = ConfirmCartItemRequest.builder()
        .reservationId(reservation.getId())
        .roomId(room.getId())
        .checkIn(reservation.getCheckIn())
        .checkOut(reservation.getCheckOut())
        .peopleNumber(reservation.getPeopleNumber())
        .build();

    when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
    when(cartRepository.findByMember(member)).thenReturn(Optional.of(cart));
    when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));
    when(reservationRepository.findOverlappingReservations(List.of(room.getId()),
        reservation.getCheckIn(), reservation.getCheckOut())).thenReturn(new ArrayList<>());

    // When
    CartConfirmResponse response = cartService.confirmReservationItem(member.getId(), request);

    // Then
    ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
    ArgumentCaptor<Reservation> reservationCaptor = ArgumentCaptor.forClass(Reservation.class);
    ArgumentCaptor<ConfirmCartItemRequest> requestCaptor = ArgumentCaptor.forClass(ConfirmCartItemRequest.class);

    verify(emailService).sendConfirmationEmail(memberCaptor.capture(), reservationCaptor.capture(), requestCaptor.capture());

    assertEquals(member.getEmail(), memberCaptor.getValue().getEmail());
    assertEquals(reservation, reservationCaptor.getValue());
    assertEquals(request, requestCaptor.getValue());

    assertNotNull(response);
    assertEquals(room.getId(), response.getRoomId());
  }
}