package com.example.mini.domain.cart.model.request;

import java.util.List;
import lombok.Data;

@Data
public class ConfirmCartItemRequest {
  private List<Long> reservationIds;
}
