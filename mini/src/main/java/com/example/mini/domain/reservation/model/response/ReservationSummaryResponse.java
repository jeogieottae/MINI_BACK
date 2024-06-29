package com.example.mini.domain.reservation.model.response;

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
public class ReservationSummaryResponse {
  private String accomodationName;
  private String accomodationAddress;
  private String roomName;
  private Integer totalPrice;
  private Integer peopleNumber;
  private LocalDateTime checkIn;
  private LocalDateTime checkOut;
}
