package com.example.mini.domain.accomodation.model.response;

import com.example.mini.domain.accomodation.entity.Accomodation;
import com.example.mini.domain.review.model.response.ReviewResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccomodationDetailsResponseDto {
    private AccomodationResponseDto accomodation;
    private List<RoomResponseDto> rooms;
    private List<ReviewResponseDto> reviews;
    private Double avgStar;
    private boolean isLiked;


    public static AccomodationDetailsResponseDto toDto(Accomodation accomodation, List<RoomResponseDto> rooms, List<ReviewResponseDto> reviews, Double avgStar, Boolean isLiked) {
        return AccomodationDetailsResponseDto.builder()
            .accomodation(AccomodationResponseDto.toDto(accomodation))
            .rooms(rooms)
            .reviews(reviews)
            .avgStar(avgStar)
            .isLiked(isLiked)
            .build();
    }
}
