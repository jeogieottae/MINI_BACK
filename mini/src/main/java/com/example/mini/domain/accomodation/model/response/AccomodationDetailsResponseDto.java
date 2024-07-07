package com.example.mini.domain.accomodation.model.response;

import com.example.mini.domain.review.model.response.ReviewResponse;
import lombok.*;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccomodationDetailsResponseDto {
    private AccomodationResponseDto accomodation;
    private List<RoomResponseDto> rooms;
    private List<ReviewResponse> reviews;
    private Double avgStar;
    private boolean isLiked;
}
