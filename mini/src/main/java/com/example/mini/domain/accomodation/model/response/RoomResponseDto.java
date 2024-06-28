package com.example.mini.domain.accomodation.model.response;

import com.example.mini.domain.accomodation.entity.Room;
import lombok.*;

@Getter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class RoomResponseDto {
    Long id;
    String name;
    int baseGuests;
    int price;
    int maxGuests;
    int extraPersonCharge;

    public static RoomResponseDto toDto(Room room) {
        return RoomResponseDto.builder()
                .id(room.getId())
                .name(room.getName())
                .baseGuests(room.getBaseGuests())
                .price(room.getPrice())
                .maxGuests(room.getMaxGuests())
                .extraPersonCharge(room.getExtraPersonCharge())
                .build();
    }
}
