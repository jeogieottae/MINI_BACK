package com.example.mini.domain.accomodation.model;

import com.example.mini.domain.accomodation.entity.Accomodation;
import com.example.mini.domain.accomodation.entity.Category;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class AccomodationResponseDto {

    private Long id;
    private String name;
    private String description;
    private int postalCode;
    private String address;
    private Boolean parkingAvailable;
    private Boolean cookingAvailable;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private Long categoryId;

    public static AccomodationResponseDto toDto(Accomodation accomodation) {
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
                .categoryId(accomodation.getCategory().getId())
                .build();
    }
}
