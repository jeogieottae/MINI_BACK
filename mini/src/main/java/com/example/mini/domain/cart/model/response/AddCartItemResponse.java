package com.example.mini.domain.cart.model.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AddCartItemResponse {

  private Long cartItemId;
  private LocalDateTime checkIn;
  private LocalDateTime checkOut;
  private Integer peopleNumber;
  private Integer price;
  private Long roomId;
}
