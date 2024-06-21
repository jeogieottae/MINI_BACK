package com.example.mini.domain.cart.model.request;

import com.example.mini.domain.reservation.entity.enums.ReservationStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReservationStatusRequest {
  private Long id;
  private ReservationStatus status;
}
