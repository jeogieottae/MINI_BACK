package com.example.mini.domain.accomodation.model.response;

import com.example.mini.domain.accomodation.entity.Accomodation;
import com.example.mini.domain.accomodation.entity.enums.AccomodationCategory;
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
    private String postalCode;
    private String address;
    private Boolean parkingAvailable;
    private Boolean cookingAvailable;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private String category;
    private Integer minPrice;

    public static AccomodationResponseDto toDto(Accomodation accomodation, Integer minPrice) {
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
                .minPrice(minPrice)
                .build();
    }
}
