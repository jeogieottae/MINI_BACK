package com.example.mini.domain.cart.model.request;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class ConfirmCartItemRequest {
  private List<ConfirmItem> confirmItems;

  @Data
  public static class ConfirmItem {
    private Long reservationId;
    private Long roomId;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
  }
}