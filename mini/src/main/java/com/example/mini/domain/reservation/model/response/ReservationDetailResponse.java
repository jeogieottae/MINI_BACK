package com.example.mini.domain.reservation.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationDetailResponse {
  private Integer roomPrice;
  private Integer baseGuests;
  private Integer extraCharge;
  private Boolean parkingAvailable;
  private Boolean cookingAvailable;
}
