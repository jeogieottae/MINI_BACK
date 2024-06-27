package com.example.mini.domain.accomodation.model.response;

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
}
