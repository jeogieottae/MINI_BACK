package com.example.mini.domain.reservation.controller;

import com.example.mini.domain.reservation.model.request.AddReservationRequest;
import com.example.mini.domain.reservation.model.response.AddReservationResponse;
import com.example.mini.domain.reservation.model.response.ReservationResponse;
import com.example.mini.domain.reservation.model.response.ReservationRoomResponse;
import com.example.mini.domain.reservation.service.ReservationService;
import com.example.mini.global.util.APIUtil;
import com.example.mini.global.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reservation")
public class ReservationController {

  @Autowired
  private ReservationService reservationService;

  @PostMapping
  public ResponseEntity<AddReservationResponse> addReservation(@RequestBody AddReservationRequest request) {
    AddReservationResponse response = reservationService.addReservation(request);
    return APIUtil.OK(response);
  }

  @GetMapping
  public ResponseEntity<List<ReservationResponse>> getAllReservationsForCurrentUser() {
    Long currentUserId = SecurityUtil.getCurrentUserId();
    List<ReservationResponse> reservations = reservationService.getAllReservationsForUser(currentUserId);
    return APIUtil.OK(reservations);
  }

  @GetMapping("/{reservationId}")
  public ResponseEntity<ReservationRoomResponse> getReservationById(@PathVariable Long reservationId) {
    ReservationRoomResponse reservation = reservationService.getReservationById(reservationId);
    return APIUtil.OK(reservation);
  }
}
