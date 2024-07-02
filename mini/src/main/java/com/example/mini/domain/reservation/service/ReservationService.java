package com.example.mini.domain.reservation.service;

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
import com.example.mini.global.api.exception.error.CartErrorCode;
import com.example.mini.global.api.exception.error.ReservationErrorCode;
import com.example.mini.global.api.exception.GlobalException;
import com.example.mini.global.email.EmailService;
import com.example.mini.global.redis.RedissonLock;
import com.example.mini.global.model.dto.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ReservationService {

  private final ReservationRepository reservationRepository;
  private final RoomRepository roomRepository;
  private final MemberRepository memberRepository;
  private final EmailService emailService;

  private final int pageSize = 10;

  @RedissonLock(key = "'confirmReservation_' + #request.roomId + '_' + #request.checkIn + '_' + #request.checkOut")
  public ReservationResponse createConfirmedReservation(Long memberId, ReservationRequest request) {
    Member member = getMember(memberId);

    validateReservation(memberId, request);

    Room room = roomRepository.findById(request.getRoomId())
        .orElseThrow(() -> new GlobalException(ReservationErrorCode.ROOM_NOT_FOUND));

    int totalPeople = request.getPeopleNumber();
    validateMaxGuests(room, totalPeople);

    int additionalCharge = calculateAdditionalCharge(room, totalPeople);

    validateDates(request.getCheckIn(), request.getCheckOut());

    validateOverlappingReservations(room.getId(), request.getCheckIn(), request.getCheckOut());

    Reservation reservation = Reservation.builder()
        .peopleNumber(request.getPeopleNumber())
        .extraCharge(additionalCharge)
        .totalPrice(room.getPrice() + additionalCharge)
        .checkIn(request.getCheckIn())
        .checkOut(request.getCheckOut())
        .accomodation(room.getAccomodation())
        .member(member)
        .room(room)
        .status(ReservationStatus.CONFIRMED)
        .build();

    reservationRepository.save(reservation);

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

    return mapToReservationResponse(reservation);
  }

  private Member getMember(Long memberId) {
    return memberRepository.findById(memberId)
        .orElseThrow(() -> new GlobalException(ReservationErrorCode.MEMBER_NOT_FOUND));
  }

  private void validateReservation(Long memberId, ReservationRequest request) {
    List<Reservation> existingReservations = reservationRepository.findOverlappingReservationsByMemberId(
        memberId, request.getRoomId(), request.getCheckIn(), request.getCheckOut());

    for (Reservation existingReservation : existingReservations) {
      if (existingReservation.getStatus() == ReservationStatus.CONFIRMED) {
        throw new GlobalException(ReservationErrorCode.DUPLICATED_RESERVATION);
      }
    }
  }

  private void validateMaxGuests(Room room, int totalPeople) {
    if (totalPeople > room.getMaxGuests()) {
      throw new GlobalException(ReservationErrorCode.EXCEEDS_MAX_GUESTS);
    }
  }

  private int calculateAdditionalCharge(Room room, int totalPeople) {
    return totalPeople > room.getBaseGuests() ? (totalPeople - room.getBaseGuests()) * room.getExtraPersonCharge() : 0;
  }

  private void validateDates(LocalDateTime checkIn, LocalDateTime checkOut) {
    if (!checkOut.isAfter(checkIn)) {
      throw new GlobalException(CartErrorCode.INVALID_CHECKOUT_DATE);
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

  private ReservationResponse mapToReservationResponse(Reservation reservation) {
    return ReservationResponse.builder()
        .roomId(reservation.getRoom().getId())
        .accomodationName(reservation.getRoom().getAccomodation().getName())
        .roomName(reservation.getRoom().getName())
        .baseGuests(reservation.getRoom().getBaseGuests())
        .maxGuests(reservation.getRoom().getMaxGuests())
        .checkIn(reservation.getCheckIn())
        .checkOut(reservation.getCheckOut())
        .peopleNumber(reservation.getPeopleNumber())
        .totalPrice(reservation.getTotalPrice())
        .build();
  }

  public PagedResponse<ReservationSummaryResponse> getAllReservations(Long memberId, int page) {
    Page<Reservation> reservations = reservationRepository.findReservationsByMemberId(
        memberId, ReservationStatus.CONFIRMED, PageRequest.of(page - 1, pageSize));

    List<ReservationSummaryResponse> content = reservations.getContent().stream()
        .map(this::mapToSummaryResponse)
        .collect(Collectors.toList());

    return new PagedResponse<>(reservations.getTotalPages(), reservations.getTotalElements(), content);
  }

  private ReservationSummaryResponse mapToSummaryResponse(Reservation reservation) {
    return ReservationSummaryResponse.builder()
        .reservationId(reservation.getId())
        .accomodationName(reservation.getRoom().getAccomodation().getName())
        .accomodationAddress(reservation.getRoom().getAccomodation().getAddress())
        .roomName(reservation.getRoom().getName())
        .totalPrice(reservation.getTotalPrice())
        .peopleNumber(reservation.getPeopleNumber())
        .checkIn(reservation.getCheckIn())
        .checkOut(reservation.getCheckOut())
        .build();
  }

  public ReservationDetailResponse getReservationDetail(Long reservationId, Long memberId) {
    Reservation reservation = reservationRepository.findByIdAndMemberId(reservationId, memberId)
        .orElseThrow(() -> new GlobalException(ReservationErrorCode.RESERVATION_NOT_FOUND));

    return mapToDetailResponse(reservation);
  }

  private ReservationDetailResponse mapToDetailResponse(Reservation reservation) {
    return ReservationDetailResponse.builder()
        .roomPrice(reservation.getRoom().getPrice())
        .baseGuests(reservation.getRoom().getBaseGuests())
        .extraCharge(reservation.getExtraCharge())
        .parkingAvailable(reservation.getRoom().getAccomodation().getParkingAvailable())
        .cookingAvailable(reservation.getRoom().getAccomodation().getCookingAvailable())
        .build();
  }
}
