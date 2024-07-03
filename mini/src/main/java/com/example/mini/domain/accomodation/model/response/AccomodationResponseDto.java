package com.example.mini.domain.accomodation.model.response;

import com.example.mini.domain.accomodation.entity.Accomodation;
import com.example.mini.domain.accomodation.entity.AccomodationImage;
import java.util.List;
import java.util.stream.Collectors;
import lombok.*;

import java.time.LocalDateTime;

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
    private List<String> imageUrls;

    public static AccomodationResponseDto toDto(Accomodation accomodation) {
        List<String> imageUrls = accomodation.getImages().stream()
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
            .imageUrls(imageUrls)
            .build();
    }
}
