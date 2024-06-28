package com.example.mini.domain.like.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccomodationResponse {
  private String name;
  private String description;
  private String postalCode;
  private String address;
}