package com.example.mini.domain.reservation.model.request;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AddReservationRequest {
  private String peopleNumber;
  private LocalDateTime checkIn;
  private LocalDateTime checkOut;
  private List<Long> roomIds;
}
