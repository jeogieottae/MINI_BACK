package com.example.mini.domain.reservation.model.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationSummaryResponse {
  private String accomodationName;
  private String accomodationAddress;
  private String roomName;
  private Integer totalPrice;
  private Integer peopleNumber;
  private LocalDateTime checkIn;
  private LocalDateTime checkOut;
}
