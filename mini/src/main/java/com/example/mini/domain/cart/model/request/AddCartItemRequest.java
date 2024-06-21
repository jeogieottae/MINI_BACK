package com.example.mini.domain.cart.model.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AddCartItemRequest {

  private Long roomId;
  private int peopleNumber;
  private LocalDateTime checkIn;
  private LocalDateTime checkOut;
}
