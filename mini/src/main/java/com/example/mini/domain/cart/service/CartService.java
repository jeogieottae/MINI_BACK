package com.example.mini.domain.cart.service;

import com.example.mini.domain.accomodation.entity.Room;
import com.example.mini.domain.cart.entity.Cart;
import com.example.mini.domain.cart.model.request.AddCartItemRequest;
import com.example.mini.domain.cart.model.request.ConfirmCartItemRequest;
import com.example.mini.domain.cart.model.request.DeleteCartItemRequest;
import com.example.mini.domain.cart.model.response.CartConfirmResponse;
import com.example.mini.domain.cart.repository.CartRepository;
import com.example.mini.domain.member.entity.Member;
import com.example.mini.domain.member.repository.MemberRepository;
import com.example.mini.domain.accomodation.repository.RoomRepository;
import com.example.mini.domain.cart.model.response.CartResponse;
import com.example.mini.domain.reservation.entity.Reservation;
import com.example.mini.domain.reservation.entity.enums.ReservationStatus;
import com.example.mini.domain.reservation.repository.ReservationRepository;
import com.example.mini.global.api.exception.error.CartErrorCode;
import com.example.mini.global.api.exception.GlobalException;
import com.example.mini.global.email.EmailService;
import com.example.mini.global.model.dto.PagedResponse;
import com.example.mini.global.redis.RedissonLock;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CartService {

  private final MemberRepository memberRepository;
  private final CartRepository cartRepository;
  private final ReservationRepository reservationRepository;
  private final RoomRepository roomRepository;
  private final EmailService emailService;

  public PagedResponse<CartResponse> getAllCartItems(Long memberId, int page) {
    Member member = getMember(memberId);
    getCartByMember(member);
    Page<Reservation> reservations = getPendingReservations(member, page);
    List<CartResponse> content = reservations.stream().map(CartResponse::toDto).toList();
    return new PagedResponse<>(reservations.getTotalPages(), reservations.getTotalElements(), content);
  }

  private Page<Reservation> getPendingReservations(Member member, int page) {
    int pageSize = 10;
    return reservationRepository.findReservationsByMemberId(
        member.getId(), ReservationStatus.PENDING, PageRequest.of(page - 1, pageSize));
  }

  private Member getMember(Long memberId) {
    return memberRepository.findById(memberId)
        .orElseThrow(() -> new GlobalException(CartErrorCode.MEMBER_NOT_FOUND));
  }

  private Cart getCartByMember(Member member) {
    return cartRepository.findByMember(member)
        .orElseThrow(() -> new GlobalException(CartErrorCode.CART_NOT_FOUND));
  }

  public void addCartItem(Long memberId, AddCartItemRequest request) {
    Member member = getMember(memberId);
    Room room = getRoomById(request.getRoomId());
    validateRequest(request, room);

    Cart cart = getOrCreateCart(member);
    validateDuplicateReservation(cart, room.getId(), request.getCheckIn(), request.getCheckOut());

    Reservation reservation = createReservation(member, room, request);
    cart.getReservationList().add(reservation);
    cartRepository.save(cart);
  }

  private Room getRoomById(Long roomId) {
    return roomRepository.findById(roomId)
        .orElseThrow(() -> new GlobalException(CartErrorCode.ROOM_NOT_FOUND));
  }

  private void validateRequest(AddCartItemRequest request, Room room) {
    if (request.getPeopleNumber() > room.getMaxGuests()) {
      throw new GlobalException(CartErrorCode.EXCEEDS_MAX_GUESTS);
    }

    if (!request.getCheckOut().isAfter(request.getCheckIn())) {
      throw new GlobalException(CartErrorCode.INVALID_CHECKOUT_DATE);
    }

    validateOverlappingReservations(room.getId(), request.getCheckIn(), request.getCheckOut());
  }

  private Cart getOrCreateCart(Member member) {
    return cartRepository.findByMember(member).orElseGet(() -> Cart.builder()
        .member(member)
        .reservationList(new ArrayList<>())
        .build());
  }

  private Reservation createReservation(Member member, Room room, AddCartItemRequest request) {
    int additionalCharge = calculateAdditionalCharge(room, request.getPeopleNumber());
    int finalPrice = room.getPrice() + additionalCharge;

    return Reservation.builder()
        .peopleNumber(request.getPeopleNumber())
        .extraCharge(additionalCharge)
        .totalPrice(finalPrice)
        .checkIn(request.getCheckIn())
        .checkOut(request.getCheckOut())
        .accomodation(room.getAccomodation())
        .member(member)
        .room(room)
        .status(ReservationStatus.PENDING)
        .build();
  }

  private void validateDuplicateReservation(Cart cart, Long roomId, LocalDateTime checkIn, LocalDateTime checkOut) {
    cart.getReservationList().forEach(existingReservation -> {
      if (existingReservation.getRoom().getId().equals(roomId) &&
          existingReservation.getCheckIn().equals(checkIn) &&
          existingReservation.getCheckOut().equals(checkOut)) {
        throw new GlobalException(CartErrorCode.DUPLICATE_RESERVATION);
      }
    });
  }

  private int calculateAdditionalCharge(Room room, int totalPeople) {
    return totalPeople > room.getBaseGuests() ? (totalPeople - room.getBaseGuests()) * room.getExtraPersonCharge() : 0;
  }

  public void deleteCartItem(Long memberId, DeleteCartItemRequest request) {
    Member member = getMember(memberId);
    Cart cart = getCartByMember(member);

    request.getReservationIds().forEach(reservationId -> {
      Reservation reservation = getReservationById(reservationId);
      validateReservationInCart(cart, reservation);
      cart.getReservationList().remove(reservation);
      reservationRepository.delete(reservation);
    });

    cartRepository.save(cart);
  }

  private Reservation getReservationById(Long reservationId) {
    return reservationRepository.findById(reservationId)
        .orElseThrow(() -> new GlobalException(CartErrorCode.RESERVATION_NOT_FOUND));
  }

  private void validateReservationInCart(Cart cart, Reservation reservation) {
    if (!cart.getReservationList().contains(reservation)) {
      throw new GlobalException(CartErrorCode.RESERVATION_NOT_IN_CART);
    }

    if (reservation.getStatus() != ReservationStatus.PENDING) {
      throw new GlobalException(CartErrorCode.RESERVATION_NOT_PENDING);
    }
  }

  @RedissonLock(key = "'confirmReservation_' + #request.roomId + '_' + #request.checkIn + '_' + #request.checkOut")
  public CartConfirmResponse confirmReservationItem(Long memberId, ConfirmCartItemRequest request) {
    Member member = getMember(memberId);
    Cart cart = getCartByMember(member);

    validateCheckoutDate(request);
    Reservation reservation = getReservationById(request.getReservationId());

    validateReservationDetails(reservation, request, member, cart);
    validateOverlappingReservations(request.getRoomId(), request.getCheckIn(), request.getCheckOut());

    if (request.getPeopleNumber() > reservation.getRoom().getMaxGuests()) {
      throw new GlobalException(CartErrorCode.EXCEEDS_MAX_GUESTS);
    }

    updateReservationDetails(request, reservation);

    emailService.sendConfirmationEmail(member, reservation, request);

    return createCartConfirmResponse(reservation, request);
  }

  private void validateCheckoutDate(ConfirmCartItemRequest request) {
    if (!request.getCheckOut().isAfter(request.getCheckIn())) {
      throw new GlobalException(CartErrorCode.INVALID_CHECKOUT_DATE);
    }
  }

  private void updateReservationDetails(ConfirmCartItemRequest request, Reservation reservation) {
    reservationRepository.updateReservationDetails(request.getPeopleNumber(), request.getCheckIn(),
        request.getCheckOut(), ReservationStatus.CONFIRMED, request.getReservationId());
  }

  private CartConfirmResponse createCartConfirmResponse(Reservation reservation, ConfirmCartItemRequest request) {
    return CartConfirmResponse.builder()
        .roomId(reservation.getRoom().getId())
        .accomodationName(reservation.getRoom().getAccomodation().getName())
        .roomName(reservation.getRoom().getName())
        .checkIn(request.getCheckIn())
        .checkOut(request.getCheckOut())
        .peopleNumber(request.getPeopleNumber())
        .totalPrice(reservation.getTotalPrice())
        .build();
  }

  private void validateReservationDetails(Reservation reservation, ConfirmCartItemRequest request, Member member, Cart cart) {
    if (!reservation.getRoom().getId().equals(request.getRoomId())) {
      throw new GlobalException(CartErrorCode.RESERVATION_MISMATCH);
    }

    if (!reservation.getMember().getId().equals(member.getId())) {
      throw new GlobalException(CartErrorCode.RESERVATION_NOT_BELONGS_TO_USER);
    }

    if (!cart.getReservationList().contains(reservation)) {
      throw new GlobalException(CartErrorCode.RESERVATION_NOT_IN_CART);
    }
  }

  private void validateOverlappingReservations(Long roomId, LocalDateTime checkIn, LocalDateTime checkOut) {
    List<Reservation> overlappingReservations = reservationRepository.findOverlappingReservations(
        List.of(roomId), checkIn, checkOut
    );

    boolean hasConflictingReservation = overlappingReservations.stream()
        .anyMatch(reservation -> reservation.getStatus() == ReservationStatus.CONFIRMED);

    if (hasConflictingReservation) {
      throw new GlobalException(CartErrorCode.CONFLICTING_RESERVATION);
    }
  }
}