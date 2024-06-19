package com.example.mini.domain.reservation.model.response;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReservationResponse {
  private Long id;
  private Integer totalPrice;
  private LocalDateTime checkIn;
  private LocalDateTime checkOut;
  private List<Long> roomIds;
  private String accomodationName;
  private String accomodationAddress;
}