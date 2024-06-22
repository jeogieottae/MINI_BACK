package com.example.mini.domain.cart.service;

import com.example.mini.domain.accomodation.entity.Room;
import com.example.mini.domain.cart.entity.Cart;
import com.example.mini.domain.cart.model.request.AddCartItemRequest;
import com.example.mini.domain.cart.model.request.DeleteCartItemRequest;
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
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CartService {

  private final MemberRepository memberRepository;
  private final CartRepository cartRepository;
  private final ReservationRepository reservationRepository;
  private final RoomRepository roomRepository;

  @Transactional
  public List<CartResponse> getAllCartItems(Long memberId) {
    Member member = getMember(memberId);
    Cart cart = cartRepository.findByMember(member).orElse(null);

    if (cart == null) {
      cart = Cart.builder()
          .member(member)
          .roomList(new ArrayList<>())
          .reservationList(new ArrayList<>())
          .build();
      cartRepository.save(cart);
      return new ArrayList<>();
    }

    List<CartResponse> cartResponses = new ArrayList<>();

    for (Reservation reservation : cart.getReservationList()) {
      if (reservation.getStatus() == ReservationStatus.PENDING) {
        Room room = reservation.getRoom();
        CartResponse cartResponse = new CartResponse(
            room.getId(),
            room.getAccomodation().getName(),
            room.getName(),
            room.getBaseGuests(),
            room.getMaxGuests(),
            reservation.getCheckIn(),
            reservation.getCheckOut(),
            reservation.getPeopleNumber(),
            reservation.getTotalPrice()
        );
        cartResponses.add(cartResponse);
      }
    }

    return cartResponses;
  }

  private Member getMember(Long memberId) {
    return memberRepository.findById(memberId)
        .orElseThrow(() -> new GlobalException(CartErrorCode.MEMBER_NOT_FOUND));
  }

  @Transactional
  public ArrayList<Object> addCartItem(Long memberId, AddCartItemRequest request) {
    Member member = getMember(memberId);

    Room room = roomRepository.findById(request.getRoomId())
        .orElseThrow(
            () -> new GlobalException(CartErrorCode.ROOM_NOT_FOUND)
        );

    int totalPeople = request.getPeopleNumber();

    if (totalPeople > room.getMaxGuests()) {
      throw new GlobalException(CartErrorCode.EXCEEDS_MAX_GUESTS);
    }

    if (!request.getCheckOut().isAfter(request.getCheckIn())) {
      throw new GlobalException(CartErrorCode.INVALID_CHECKOUT_DATE);
    }

    List<Reservation> overlappingReservations = reservationRepository.findOverlappingReservations(
        List.of(room.getId()), request.getCheckIn(), request.getCheckOut()
    );

    for (Reservation overlappingReservation : overlappingReservations) {
      if (overlappingReservation.getStatus() == ReservationStatus.CONFIRMED) {
        throw new GlobalException(CartErrorCode.CONFLICTING_RESERVATION);
      }
    }

    Cart cart = cartRepository.findByMember(member).orElse(null);

    if (cart != null) {
      for (Reservation existingReservation : cart.getReservationList()) {
        if (existingReservation.getRoom().getId().equals(room.getId()) &&
            existingReservation.getCheckIn().equals(request.getCheckIn()) &&
            existingReservation.getCheckOut().equals(request.getCheckOut())) {
          throw new GlobalException(CartErrorCode.DUPLICATE_RESERVATION);
        }
      }
    } else {
      cart = Cart.builder()
          .member(member)
          .reservationList(new ArrayList<>())
          .build();
    }

    int additionalCharge = 0;
    if (totalPeople > room.getBaseGuests()) {
      additionalCharge = (totalPeople - room.getBaseGuests()) * room.getExtraPersonCharge();
    }

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
    return new ArrayList<>();
  }

  @Transactional
  public void deleteCartItem(Long memberId, DeleteCartItemRequest request) {
    Member member = getMember(memberId);

    List<Long> reservationIds = request.getReservationIds();

    Cart cart = cartRepository.findByMember(member)
        .orElseThrow(() -> new GlobalException(CartErrorCode.CART_NOT_FOUND));

    for (Long reservationId : reservationIds) {
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

  @Transactional
  public void confirmCartItems(Long memberId, List<Long> reservationIds) {
    Member member = getMember(memberId);

    Cart cart = cartRepository.findByMember(member)
        .orElseThrow(() -> new GlobalException(CartErrorCode.CART_NOT_FOUND));

    for (Long reservationId : reservationIds) {
      Reservation reservation = reservationRepository.findById(reservationId)
          .orElseThrow(() -> new GlobalException(CartErrorCode.RESERVATION_NOT_FOUND));

      if (!reservation.getMember().getId().equals(memberId)) {
        throw new GlobalException(CartErrorCode.RESERVATION_NOT_BELONGS_TO_USER);
      }

      if (!cart.getReservationList().contains(reservation)) {
        throw new GlobalException(CartErrorCode.RESERVATION_NOT_IN_CART);
      }

      if (reservation.getStatus() != ReservationStatus.PENDING) {
        throw new GlobalException(CartErrorCode.RESERVATION_NOT_PENDING);
      }

      reservationRepository.updateReservationStatus(reservationId, ReservationStatus.CONFIRMED);
    }
  }
}