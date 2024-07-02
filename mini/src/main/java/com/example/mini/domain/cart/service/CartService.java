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
import com.example.mini.global.api.exception.error.ReservationErrorCode;
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

  private final int pageSize = 10;

  public PagedResponse<CartResponse> getAllCartItems(Long memberId, int page) {
    Member member = getMember(memberId);
    cartRepository.findByMember(member)
        .orElseThrow(() -> new GlobalException(CartErrorCode.RESERVATION_NOT_FOUND));

    Page<Reservation> reservations = reservationRepository.findReservationsByMemberId(
        member.getId(), ReservationStatus.PENDING, PageRequest.of(page - 1, pageSize));
    List<CartResponse> content = reservations.stream().map(CartResponse::toDto).toList();
    return new PagedResponse<>(reservations.getTotalPages(), reservations.getTotalElements(), content);
  }

  private Member getMember(Long memberId) {
    return memberRepository.findById(memberId)
        .orElseThrow(() -> new GlobalException(CartErrorCode.MEMBER_NOT_FOUND));
  }

  public List<Object> addCartItem(Long memberId, AddCartItemRequest request) {
    Member member = getMember(memberId);

    Room room = roomRepository.findById(request.getRoomId())
        .orElseThrow(() -> new GlobalException(CartErrorCode.ROOM_NOT_FOUND));

    int totalPeople = request.getPeopleNumber();

    if (totalPeople > room.getMaxGuests()) {
      throw new GlobalException(CartErrorCode.EXCEEDS_MAX_GUESTS);
    }

    if (!request.getCheckOut().isAfter(request.getCheckIn())) {
      throw new GlobalException(CartErrorCode.INVALID_CHECKOUT_DATE);
    }

    validateOverlappingReservations(room.getId(), request.getCheckIn(), request.getCheckOut());

    Cart cart = cartRepository.findByMember(member).orElse(null);

    if (cart != null) {
      validateDuplicateReservation(cart, room.getId(), request.getCheckIn(), request.getCheckOut());
    } else {
      cart = Cart.builder()
          .member(member)
          .reservationList(new ArrayList<>())
          .build();
    }

    int additionalCharge = calculateAdditionalCharge(room, totalPeople);
    int finalPrice = room.getPrice() + additionalCharge;

    Reservation reservation = Reservation.builder()
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

    cart.getReservationList().add(reservation);
    cartRepository.save(cart);
    return List.of();
  }

  private void validateDuplicateReservation(Cart cart, Long roomId, LocalDateTime checkIn, LocalDateTime checkOut) {
    for (Reservation existingReservation : cart.getReservationList()) {
      if (existingReservation.getRoom().getId().equals(roomId) &&
          existingReservation.getCheckIn().equals(checkIn) &&
          existingReservation.getCheckOut().equals(checkOut)) {
        throw new GlobalException(CartErrorCode.DUPLICATE_RESERVATION);
      }
    }
  }

  private int calculateAdditionalCharge(Room room, int totalPeople) {
    return totalPeople > room.getBaseGuests() ? (totalPeople - room.getBaseGuests()) * room.getExtraPersonCharge() : 0;
  }

  public void deleteCartItem(Long memberId, DeleteCartItemRequest request) {
    Member member = getMember(memberId);

    Cart cart = cartRepository.findByMember(member)
        .orElseThrow(() -> new GlobalException(CartErrorCode.CART_NOT_FOUND));

    for (Long reservationId : request.getReservationIds()) {
      Reservation reservation = reservationRepository.findById(reservationId)
          .orElseThrow(() -> new GlobalException(CartErrorCode.RESERVATION_NOT_FOUND));

      if (!cart.getReservationList().contains(reservation)) {
        throw new GlobalException(CartErrorCode.RESERVATION_NOT_IN_CART);
      }

      if (reservation.getStatus() != ReservationStatus.PENDING) {
        throw new GlobalException(CartErrorCode.RESERVATION_NOT_PENDING);
      }

      cart.getReservationList().remove(reservation);
      reservationRepository.delete(reservation);
    }

    cartRepository.save(cart);
  }

  @RedissonLock(key = "'confirmReservation_' + #request.roomId + '_' + #request.checkIn + '_' + #request.checkOut")
  public CartConfirmResponse confirmReservationItem(Long memberId, ConfirmCartItemRequest request) {
    Member member = getMember(memberId);

    Cart cart = cartRepository.findByMember(member)
        .orElseThrow(() -> new GlobalException(CartErrorCode.CART_NOT_FOUND));

    if (!request.getCheckOut().isAfter(request.getCheckIn())) {
      throw new GlobalException(CartErrorCode.INVALID_CHECKOUT_DATE);
    }

    Reservation reservation = reservationRepository.findById(request.getReservationId())
        .orElseThrow(() -> new GlobalException(CartErrorCode.RESERVATION_NOT_FOUND));

    validateReservationDetails(reservation, request, member, cart);

    validateOverlappingReservations(request.getRoomId(), request.getCheckIn(), request.getCheckOut());

    if (request.getPeopleNumber() > reservation.getRoom().getMaxGuests()) {
      throw new GlobalException(CartErrorCode.EXCEEDS_MAX_GUESTS);
    }

    reservationRepository.updateReservationDetails(request.getPeopleNumber(), request.getCheckIn(),
        request.getCheckOut(), ReservationStatus.CONFIRMED, request.getReservationId());

    sendConfirmationEmail(member, reservation, request);

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
      throw new GlobalException(ReservationErrorCode.CONFLICTING_RESERVATION);
    }
  }

  private void sendConfirmationEmail(Member member, Reservation reservation, ConfirmCartItemRequest request) {
    String to = member.getEmail();
    String subject = "예약 확정 되었습니다";
    String text = String.format("귀하의 %s에서 %s 객실 예약이 확정되었습니다.\n체크인: %s\n체크아웃: %s\n인원 수: %d명\n총 가격: %d원",
        reservation.getRoom().getAccomodation().getName(),
        reservation.getRoom().getName(),
        request.getCheckIn(),
        request.getCheckOut(),
        request.getPeopleNumber(),
        reservation.getTotalPrice());

    emailService.sendReservationConfirmationEmail(to, subject, text);
  }
}
