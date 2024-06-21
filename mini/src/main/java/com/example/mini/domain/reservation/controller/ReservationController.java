package com.example.mini.domain.reservation.controller;

import com.example.mini.domain.reservation.model.request.ReservationRequest;
import com.example.mini.domain.reservation.model.response.ReservationResponse;
import com.example.mini.domain.reservation.service.ReservationService;
import com.example.mini.global.security.details.UserDetailsImpl;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservation")
public class ReservationController {

  private final ReservationService reservationService;

  @Autowired
  public ReservationController(ReservationService reservationService) {
    this.reservationService = reservationService;
  }

  @PostMapping
  public ResponseEntity<ReservationResponse> confirmReservation(@RequestBody ReservationRequest request,
      @AuthenticationPrincipal UserDetailsImpl userDetails) {
    Long memberId = userDetails.getMemberId();
    ReservationResponse response = reservationService.createConfirmedReservation(memberId, request);
    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }

//  @GetMapping
//  public ResponseEntity<List<ReservationResponse>> getConfirmedReservations() {
//    List<ReservationResponse> reservations = reservationService.getConfirmedReservations();
//    return ResponseEntity.ok(reservations);
//  }

}
