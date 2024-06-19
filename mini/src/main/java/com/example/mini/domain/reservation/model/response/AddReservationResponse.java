package com.example.mini.domain.reservation.model.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AddReservationResponse {
  private Long id;
  private String peopleNumber;
  private Integer extraCharge;
  private Integer totalPrice;
  private LocalDateTime checkIn;
  private LocalDateTime checkOut;
  private List<Long> roomIds;
}
