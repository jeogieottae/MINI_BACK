package com.example.mini.domain.reservation.model.request;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ReservationRequest {
  private Long roomId;
  private LocalDateTime checkIn;
  private LocalDateTime checkOut;
  private int peopleNumber;
}