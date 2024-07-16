package com.example.mini.domain.accomodation.model.response;

import com.example.mini.domain.accomodation.entity.Accomodation;
import com.example.mini.domain.accomodation.entity.AccomodationImage;
import java.util.List;
import java.util.stream.Collectors;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    private boolean isLiked;
    private List<String> accomodationImageUrls;

    public static AccomodationCardResponseDto toDto(Accomodation accomodation, Integer minPrice, boolean reservationAvailable, boolean isLiked) {
        int likeCount = 0;
        if (accomodation.getLikes() != null) {
            likeCount = accomodation.getLikes().size();
        }

        List<String> accomodationImageUrls = accomodation.getImages().stream()
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
            .accomodationImageUrls(accomodationImageUrls)
            .isLiked(isLiked)
            .build();
    }
}