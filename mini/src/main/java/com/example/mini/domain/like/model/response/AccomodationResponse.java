package com.example.mini.domain.like.model.response;

import lombok.Data;

@Data
public class AccomodationResponse {
  private String name;
  private String description;
  private int postalCode;
  private String address;
}