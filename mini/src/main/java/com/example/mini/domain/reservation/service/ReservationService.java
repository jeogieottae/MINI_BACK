package com.example.mini.domain.reservation.service;

import com.example.mini.domain.accomodation.entity.Room;
import com.example.mini.domain.member.entity.Member;
import com.example.mini.domain.reservation.entity.Reservation;
import com.example.mini.domain.reservation.entity.enums.ReservationStatus;
import com.example.mini.domain.reservation.model.request.ReservationRequest;
import com.example.mini.domain.reservation.model.response.ReservationCancelResponse;
import com.example.mini.domain.reservation.model.response.ReservationDetailResponse;
import com.example.mini.domain.reservation.model.response.ReservationResponse;
import com.example.mini.domain.reservation.model.response.ReservationSummaryResponse;
import com.example.mini.domain.reservation.repository.ReservationRepository;
import com.example.mini.domain.accomodation.repository.RoomRepository;
import com.example.mini.domain.member.repository.MemberRepository;
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

    emailService.sendReservationConfirmationEmail(member, reservation, request);

    return ReservationResponse.toDto(reservation);
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
      throw new GlobalException(ReservationErrorCode.INVALID_CHECKOUT_DATE);
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

  public PagedResponse<ReservationSummaryResponse> getAllReservations(Long memberId, int page) {
    getMember(memberId);
    int pageSize = 10;
    Page<Reservation> reservations = reservationRepository.findReservationsByMemberId(
        memberId, ReservationStatus.CONFIRMED, PageRequest.of(page - 1, pageSize));

    List<ReservationSummaryResponse> content = reservations.getContent().stream()
        .map(ReservationSummaryResponse::toDto)
        .collect(Collectors.toList());

    return new PagedResponse<>(reservations.getTotalPages(), reservations.getTotalElements(), content);
  }

  public ReservationDetailResponse getReservationDetail(Long reservationId, Long memberId) {
    Reservation reservation = reservationRepository.findByIdAndMemberId(reservationId, memberId)
        .orElseThrow(() -> new GlobalException(ReservationErrorCode.RESERVATION_NOT_FOUND));

    return ReservationDetailResponse.toDto(reservation);
  }

  public ReservationCancelResponse cancelReservation(Long reservationId, Long memberId) {
    getMember(memberId);
    Reservation reservation = reservationRepository.findByIdAndMemberId(reservationId, memberId)
        .orElseThrow(() -> new GlobalException(ReservationErrorCode.RESERVATION_NOT_FOUND));

    if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
      throw new GlobalException(ReservationErrorCode.INVALID_RESERVATION_STATUS);
    }

    cancelReservationDetails(reservationId);

    return ReservationCancelResponse.toDto(reservation);
  }

  private void cancelReservationDetails(Long reservationId) {
    reservationRepository.cancelReservation(reservationId, ReservationStatus.CANCELED);
  }

  public PagedResponse<ReservationCancelResponse> getcanceledReservation(Long memberId, int page) {
    getMember(memberId);
    int pageSize = 10;

    Page<Reservation> reservations = reservationRepository.findReservationsByMemberId(
        memberId, ReservationStatus.CANCELED, PageRequest.of(page - 1, pageSize));

    List<ReservationCancelResponse> content = reservations.getContent().stream()
        .map(ReservationCancelResponse::toDto)
        .collect(Collectors.toList());

    return new PagedResponse<>(reservations.getTotalPages(), reservations.getTotalElements(), content);
  }
}
