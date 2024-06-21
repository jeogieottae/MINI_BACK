package com.example.mini.domain.reservation.controller;

import com.example.mini.domain.reservation.model.request.AddReservationRequest;
import com.example.mini.domain.reservation.model.response.AddReservationResponse;
import com.example.mini.domain.reservation.model.response.ReservationResponse;
import com.example.mini.domain.reservation.model.response.ReservationRoomResponse;
import com.example.mini.domain.reservation.service.ReservationService;
import com.example.mini.global.api.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reservation")
public class ReservationController {

  @Autowired
  private ReservationService reservationService;

  @PostMapping
  public ResponseEntity<ApiResponse<AddReservationResponse>> addReservation(@RequestBody AddReservationRequest request) {
    AddReservationResponse response = reservationService.addReservation(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.CREATED(response));
  }

  @GetMapping
  public ResponseEntity<ApiResponse<List<ReservationResponse>>> getAllReservationsForCurrentUser() {
//    Long currentUserId = SecurityUtil.getCurrentUserId();
//    List<ReservationResponse> reservations = reservationService.getAllReservationsForUser(currentUserId);
//    return ResponseEntity.ok(ApiResponse.OK(reservations));
  return ResponseEntity.ok().build();
  }

//  @GetMapping("/{reservationId}")
//  public ResponseEntity<ApiResponse<ReservationRoomResponse>> getReservationById(@PathVariable Long reservationId) {
//    ReservationRoomResponse reservation = reservationService.getReservationById(reservationId);
//    return ResponseEntity.ok(ApiResponse.OK(reservation));
//  }
}
