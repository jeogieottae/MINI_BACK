package com.example.mini.domain.accomodation.model.response;

import com.example.mini.domain.accomodation.entity.Accomodation;
import com.example.mini.domain.accomodation.entity.AccomodationImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccomodationResponseDto {

    private Long id;
    private String name;
    private String description;
    private String postalCode;
    private String address;
    private Boolean parkingAvailable;
    private Boolean cookingAvailable;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private String category;
    private List<String> accomodationImageUrls;

    public static AccomodationResponseDto toDto(Accomodation accomodation) {
        List<String> accomodationImageUrls = accomodation.getImages().stream()
            .map(AccomodationImage::getImgUrl)
            .collect(Collectors.toList());

        return AccomodationResponseDto.builder()
            .id(accomodation.getId())
            .name(accomodation.getName())
            .description(accomodation.getDescription())
            .postalCode(accomodation.getPostalCode())
            .address(accomodation.getAddress())
            .parkingAvailable(accomodation.getParkingAvailable())
            .cookingAvailable(accomodation.getCookingAvailable())
            .checkIn(accomodation.getCheckIn())
            .checkOut(accomodation.getCheckOut())
            .category(accomodation.getCategory().getName())
            .accomodationImageUrls(accomodationImageUrls)
            .build();
    }
}