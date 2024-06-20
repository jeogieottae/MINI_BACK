package com.example.mini.domain.reservation.model.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReservationAccomodationResponse {
  private Long id;
  private String name;
  private String address;
}