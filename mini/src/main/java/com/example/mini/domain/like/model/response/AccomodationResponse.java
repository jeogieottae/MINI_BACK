package com.example.mini.domain.like.model.response;

import com.example.mini.domain.accomodation.entity.Accomodation;

import java.util.List;
import java.util.stream.Collectors;
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
    private List<String> imageUrls;


    public static AccomodationResponse toDto(Accomodation accomodation) {
        List<String> imageUrls = accomodation.getImages().stream()
            .map(image -> image.getImgUrl())
            .collect(Collectors.toList());

        return AccomodationResponse.builder()
            .name(accomodation.getName())
            .description(accomodation.getDescription())
            .postalCode(accomodation.getPostalCode())
            .address(accomodation.getAddress())
            .imageUrls(imageUrls)
            .build();
    }
}