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
public class AccomodationCardResponseDto {
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
    private boolean reservationAvailable;
    private int likeCount;
    private List<String> imageUrls;

    public static AccomodationCardResponseDto toDto(Accomodation accomodation, Integer minPrice, boolean reservationAvailable) {
        int likeCount = 0;
        if (accomodation.getLikes() != null) {
            likeCount = accomodation.getLikes().size();
        }

        List<String> imageUrls = accomodation.getImages().stream()
            .map(AccomodationImage::getImgUrl)
            .collect(Collectors.toList());

        return AccomodationCardResponseDto.builder()
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
            .reservationAvailable(reservationAvailable)
            .likeCount(likeCount)
            .imageUrls(imageUrls)
            .build();
    }
}
