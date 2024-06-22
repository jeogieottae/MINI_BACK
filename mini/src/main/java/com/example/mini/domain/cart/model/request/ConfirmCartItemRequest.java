package com.example.mini.domain.cart.model.request;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ConfirmCartItemRequest {
  private Long reservationId;
  private Long roomId;
  private LocalDateTime checkIn;
  private LocalDateTime checkOut;
}