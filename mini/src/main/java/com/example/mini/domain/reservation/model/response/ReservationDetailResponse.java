package com.example.mini.domain.reservation.model.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationDetailResponse {
  private String memberName;
  private String accomodationName;
  private String roomName;
  private Integer roomPrice;
  private Integer baseGuests;
  private Integer extraCharge;
  private LocalDateTime checkIn;
  private LocalDateTime checkOut;
  private Boolean parkingAvailable;
  private Boolean cookingAvailable;
}