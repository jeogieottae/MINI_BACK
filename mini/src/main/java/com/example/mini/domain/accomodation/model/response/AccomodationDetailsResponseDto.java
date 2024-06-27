package com.example.mini.domain.accomodation.model.response;

import com.example.mini.domain.review.model.response.ReviewResponse;
import lombok.*;

import java.util.List;

@Getter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class AccomodationDetailsResponseDto {
    AccomodationResponseDto accomodation;
    List<RoomResponseDto> rooms;
    List<ReviewResponse> reviews;
    Double avgStar;
}
