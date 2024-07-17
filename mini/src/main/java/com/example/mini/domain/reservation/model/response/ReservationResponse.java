package com.example.mini.domain.reservation.model.response;

import com.example.mini.domain.reservation.entity.Reservation;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponse implements Serializable {
  private Long roomId;
  private String accomodationName;
  private String roomName;
  private Integer baseGuests;
  private Integer maxGuests;
  private LocalDateTime checkIn;
  private LocalDateTime checkOut;
  private Integer peopleNumber;
  private Integer totalPrice;

  public static ReservationResponse toDto(Reservation reservation) {
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

}