package com.example.mini.domain.reservation.service;

import com.example.mini.domain.accomodation.entity.Room;
import com.example.mini.domain.member.entity.Member;
import com.example.mini.domain.reservation.model.request.ReservationRequest;
import com.example.mini.domain.reservation.model.response.ReservationDetailResponse;
import com.example.mini.domain.reservation.model.response.ReservationResponse;
import com.example.mini.domain.reservation.entity.Reservation;
import com.example.mini.domain.reservation.entity.enums.ReservationStatus;
import com.example.mini.domain.reservation.model.response.ReservationSummaryResponse;
import com.example.mini.domain.reservation.repository.ReservationRepository;
import com.example.mini.domain.accomodation.repository.RoomRepository;
import com.example.mini.domain.member.repository.MemberRepository;
import com.example.mini.global.api.exception.error.CartErrorCode;
import com.example.mini.global.api.exception.error.ReservationErrorCode;
import com.example.mini.global.api.exception.GlobalException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    if (!request.getCheckOut().isAfter(request.getCheckIn())) {
      throw new GlobalException(CartErrorCode.INVALID_CHECKOUT_DATE);
    }

    List<Reservation> overlappingReservations = reservationRepository.findOverlappingReservations(
        List.of(room.getId()), request.getCheckIn(), request.getCheckOut()
    );

    for (Reservation overlappingReservation : overlappingReservations) {
      if (overlappingReservation.getStatus() == ReservationStatus.CONFIRMED) {
        throw new GlobalException(ReservationErrorCode.CONFLICTING_RESERVATION);
      }
    }

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

  public Page<ReservationSummaryResponse> getAllReservations(Long memberId, Pageable pageable) {
    Page<Reservation> reservations = reservationRepository.findReservationsByMemberId(memberId, ReservationStatus.CONFIRMED, pageable);
    return reservations.map(this::mapToSummaryResponse);
  }

  private ReservationSummaryResponse mapToSummaryResponse(Reservation reservation) {
    return new ReservationSummaryResponse(
        reservation.getRoom().getAccomodation().getName(),
        reservation.getRoom().getAccomodation().getAddress(),
        reservation.getRoom().getName(),
        reservation.getTotalPrice(),
        reservation.getPeopleNumber(),
        reservation.getCheckIn(),
        reservation.getCheckOut()
    );
  }

  public ReservationDetailResponse getReservationDetail(Long reservationId, Long memberId) {
    Reservation reservation = reservationRepository.findByIdAndMemberId(reservationId, memberId)
        .orElseThrow(() -> new GlobalException(ReservationErrorCode.RESERVATION_NOT_FOUND));

    return mapToDetailResponse(reservation);
  }

  private ReservationDetailResponse mapToDetailResponse(Reservation reservation) {
    int roomPrice = reservation.getRoom().getPrice();
    int baseGuests = reservation.getRoom().getBaseGuests();
    int totalGuests = reservation.getPeopleNumber();
    int extraPersonCharge = reservation.getRoom().getExtraPersonCharge();

    int extraCharge = 0;

    if (totalGuests > baseGuests) {
      extraCharge = (totalGuests - baseGuests) * extraPersonCharge;
    }

    boolean parkingAvailable = reservation.getRoom().getAccomodation().getParkingAvailable();
    boolean cookingAvailable = reservation.getRoom().getAccomodation().getCookingAvailable();

    return new ReservationDetailResponse(
        roomPrice,
        baseGuests,
        extraCharge,
        parkingAvailable,
        cookingAvailable
    );
  }
}
