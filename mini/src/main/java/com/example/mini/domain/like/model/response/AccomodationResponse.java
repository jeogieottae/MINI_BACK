package com.example.mini.domain.like.model.response;

import com.example.mini.domain.accomodation.entity.Accomodation;
import com.example.mini.domain.like.entity.Like;
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

    public static AccomodationResponse toDto(Accomodation accomodation) {
      return AccomodationResponse.builder()
              .name(accomodation.getName())
              .description(accomodation.getDescription())
              .postalCode(accomodation.getPostalCode())
              .address(accomodation.getAddress())
              .build();
    }
}