package com.example.mini.domain.like.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccomodationResponse {
  private String name;
  private String description;
  private int postalCode;
  private String address;
}