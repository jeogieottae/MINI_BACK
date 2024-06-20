package com.example.mini.domain.cart.model.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AddCartItemRequest {

  private LocalDateTime checkIn;
  private LocalDateTime checkOut;
  private Integer peopleNumber;
  private Integer price;
  private Long roomId;
}
