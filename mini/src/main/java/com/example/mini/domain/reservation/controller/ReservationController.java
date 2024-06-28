package com.example.mini.domain.reservation.controller;

import com.example.mini.domain.reservation.model.request.ReservationRequest;
import com.example.mini.domain.reservation.model.response.ReservationDetailResponse;
import com.example.mini.domain.reservation.model.response.ReservationResponse;
import com.example.mini.domain.reservation.model.response.ReservationSummaryResponse;
import com.example.mini.domain.reservation.service.ReservationService;
import com.example.mini.global.api.ApiResponse;
import com.example.mini.global.security.details.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservation")
@RequiredArgsConstructor
public class ReservationController {

  private final ReservationService reservationService;

  @PostMapping
  public ResponseEntity<ReservationResponse> confirmReservation(
      @RequestBody ReservationRequest request,
      @AuthenticationPrincipal UserDetailsImpl userDetails) {
    Long memberId = userDetails.getMemberId();
    ReservationResponse response = reservationService.createConfirmedReservation(memberId, request);
    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }

  @GetMapping
  public ResponseEntity<ApiResponse<Page<ReservationSummaryResponse>>> getAllReservations(
      @AuthenticationPrincipal UserDetailsImpl userDetails,
      @PageableDefault(size = 10) Pageable pageable
  ) {
    Long memberId = userDetails.getMemberId();
    Page<ReservationSummaryResponse> reservations = reservationService.getAllReservations(memberId,
        pageable);
    return ResponseEntity.ok(ApiResponse.OK(reservations));
  }

  @GetMapping("/detail")
  public ResponseEntity<ReservationDetailResponse> getReservationDetail(
      @RequestParam("reservationId") Long reservationId,
      @AuthenticationPrincipal UserDetailsImpl userDetails) {

    Long memberId = userDetails.getMemberId();
    ReservationDetailResponse reservationDetail = reservationService.getReservationDetail(
        reservationId, memberId);
    return ResponseEntity.ok(reservationDetail);
  }
}