package com.example.mini.domain.cart.model.response;

import java.time.LocalDateTime;

import com.example.mini.domain.reservation.entity.Reservation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {

  private Long roomId;
  private String accommodationName;
  private String roomName;
  private Integer baseGuests;
  private Integer maxGuests;
  private LocalDateTime checkIn;
  private LocalDateTime checkOut;
  private Integer peopleNumber;
  private Integer totalPrice;

  public static CartResponse toDto(Reservation reservation) {
    return CartResponse.builder()
            .roomId(reservation.getRoom().getId())
            .accommodationName(reservation.getAccomodation().getName())
            .roomName(reservation.getRoom().getName())
            .baseGuests(reservation.getRoom().getBaseGuests())
            .maxGuests(reservation.getRoom().getMaxGuests())
            .checkIn(reservation.getCheckIn())
            .checkOut(reservation.getCheckOut())
            .peopleNumber(reservation.getPeopleNumber())
            .totalPrice(reservation.getTotalPrice())
            .build();
  }
}