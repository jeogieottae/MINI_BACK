package com.example.mini.domain.cart.model.request;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmCartItemRequest {
  private Long reservationId;
  private Long roomId;
  private int peopleNumber;
  private LocalDateTime checkIn;
  private LocalDateTime checkOut;
}
