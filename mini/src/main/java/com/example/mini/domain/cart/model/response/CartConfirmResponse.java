package com.example.mini.domain.cart.model.response;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CartConfirmResponse implements Serializable {
  private Long roomId;
  private String accomodationName;
  private String roomName;
  private LocalDateTime checkIn;
  private LocalDateTime checkOut;
  private Integer peopleNumber;
  private Integer totalPrice;
}
