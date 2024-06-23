package com.example.mini.domain.reservation.model.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReservationResponse {
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