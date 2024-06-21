package com.example.mini.domain.reservation.service;

import com.example.mini.domain.accomodation.entity.Room;
import com.example.mini.domain.cart.model.response.CartResponse;
import com.example.mini.domain.member.entity.Member;
import com.example.mini.domain.reservation.model.request.ReservationRequest;
import com.example.mini.domain.reservation.model.response.ReservationResponse;
import com.example.mini.domain.reservation.entity.Reservation;
import com.example.mini.domain.reservation.entity.enums.ReservationStatus;
import com.example.mini.domain.reservation.repository.ReservationRepository;
import com.example.mini.domain.accomodation.repository.RoomRepository;
import com.example.mini.domain.member.repository.MemberRepository;
import com.example.mini.global.api.exception.error.CartErrorCode;
import com.example.mini.global.api.exception.error.ReservationErrorCode;
import com.example.mini.global.api.exception.GlobalException;
import com.example.mini.global.security.details.UserDetailsImpl;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ReservationService {

  private final ReservationRepository reservationRepository;
  private final RoomRepository roomRepository;
  private final MemberRepository memberRepository;

  public ReservationResponse createConfirmedReservation(Long memberId, ReservationRequest request) {
    Member member = getMember(memberId);

    Room room = roomRepository.findById(request.getRoomId())
        .orElseThrow(() -> new GlobalException(ReservationErrorCode.ROOM_NOT_FOUND));

    int totalPeople = request.getPeopleNumber();
    if (totalPeople > room.getMaxGuests()) {
      throw new GlobalException(ReservationErrorCode.EXCEEDS_MAX_GUESTS);
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
        .status(ReservationStatus.CONFIRMED)
        .build();

    reservationRepository.save(reservation);

    return ReservationResponse.builder()
        .roomId(room.getId())
        .accommodationName(room.getAccomodation().getName())
        .roomName(room.getName())
        .baseGuests(room.getBaseGuests())
        .maxGuests(room.getMaxGuests())
        .checkIn(reservation.getCheckIn())
        .checkOut(reservation.getCheckOut())
        .peopleNumber(reservation.getPeopleNumber())
        .totalPrice(reservation.getTotalPrice())
        .build();
  }

  private Member getMember(Long memberId) {
    return memberRepository.findById(memberId)
        .orElseThrow(() -> new GlobalException(ReservationErrorCode.MEMBER_NOT_FOUND));
  }

//
//  @Transactional(readOnly = true)
//  public List<ReservationResponse> getConfirmedReservations() {
//    List<Reservation> confirmedReservations = reservationRepository.findByStatus(ReservationStatus.CONFIRMED);
//    List<ReservationResponse> reservationResponses = new ArrayList<>();
//
//    for (Reservation reservation : confirmedReservations) {
//      reservationResponses.add(new ReservationResponse(
//          reservation.getId(),
//          reservation.getCheckIn(),
//          reservation.getCheckOut(),
//          reservation.getPeopleNumber(),
//          reservation.getTotalPrice(),
//          reservation.getStatus()
//      ));
//    }
//
//    return reservationResponses;
//  }

}
