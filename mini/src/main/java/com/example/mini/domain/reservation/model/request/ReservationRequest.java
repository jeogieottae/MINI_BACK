package com.example.mini.domain.reservation.model.request;

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
public class ReservationRequest {
  private Long roomId;
  private LocalDateTime checkIn;
  private LocalDateTime checkOut;
  private int peopleNumber;
}