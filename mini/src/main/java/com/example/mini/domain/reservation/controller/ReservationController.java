package com.example.mini.domain.reservation.controller;

import com.example.mini.domain.reservation.model.request.ReservationRequest;
import com.example.mini.domain.reservation.model.response.ReservationDetailResponse;
import com.example.mini.domain.reservation.model.response.ReservationResponse;
import com.example.mini.domain.reservation.model.response.ReservationSummaryResponse;
import com.example.mini.domain.reservation.service.ReservationService;
import com.example.mini.global.api.ApiResponse;
import com.example.mini.global.api.exception.success.SuccessCode;
import com.example.mini.global.model.dto.PagedResponse;
import com.example.mini.global.security.details.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservation")
@RequiredArgsConstructor
public class ReservationController {

  private final ReservationService reservationService;

  @PostMapping
  public ResponseEntity<ApiResponse<ReservationResponse>> confirmReservation(
      @RequestBody ReservationRequest request,
      @AuthenticationPrincipal UserDetailsImpl userDetails) {
    Long memberId = userDetails.getMemberId();
    ReservationResponse response = reservationService.createConfirmedReservation(memberId, request);
    return ResponseEntity.ok(ApiResponse.SUCCESS(SuccessCode.RESERVATION_CONFIRMED, response));
  }

  @GetMapping
  public ResponseEntity<ApiResponse<PagedResponse<ReservationSummaryResponse>>> getAllReservations(
      @AuthenticationPrincipal UserDetailsImpl userDetails,
      @RequestParam(value = "page", defaultValue = "1") int page
  ) {
    PagedResponse<ReservationSummaryResponse> reservations = reservationService.getAllReservations(userDetails.getMemberId(), page);
    return ResponseEntity.ok(ApiResponse.SUCCESS(SuccessCode.RESERVATIONS_RETRIEVED, reservations));
  }

  @GetMapping("/detail/{reservationId}")
  public ResponseEntity<ApiResponse<ReservationDetailResponse>> getReservationDetail(
      @PathVariable("reservationId") Long reservationId,
      @AuthenticationPrincipal UserDetailsImpl userDetails) {

    Long memberId = userDetails.getMemberId();
    ReservationDetailResponse reservationDetail = reservationService.getReservationDetail(
        reservationId, memberId);
    return ResponseEntity.ok(ApiResponse.SUCCESS(SuccessCode.RESERVATION_DETAIL_RETRIEVED, reservationDetail));
  }
}