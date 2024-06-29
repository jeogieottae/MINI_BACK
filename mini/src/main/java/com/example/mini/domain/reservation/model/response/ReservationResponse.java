package com.example.mini.domain.reservation.model.response;

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

}