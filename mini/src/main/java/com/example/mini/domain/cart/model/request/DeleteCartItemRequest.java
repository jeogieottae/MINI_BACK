package com.example.mini.domain.cart.model.request;

import lombok.Data;

import java.util.List;

@Data
public class DeleteCartItemRequest {
  private List<Long> reservationIds;
}
