package com.example.mini.domain.cart.model.request;

import com.example.mini.domain.cart.model.request.ConfirmCartItemRequest.ConfirmItem;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data

public class ConfirmCartItemRequestTest {
  private List<ConfirmItemtest> confirmItemstest;

  @Data
  public static class ConfirmItemtest {
    private Long roomId;
    private int peopleNumber;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
  }

}
