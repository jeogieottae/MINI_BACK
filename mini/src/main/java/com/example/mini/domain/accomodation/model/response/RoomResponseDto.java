package com.example.mini.domain.accomodation.model.response;

import com.example.mini.domain.accomodation.entity.Room;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoomResponseDto {
    Long id;
    String name;
    int baseGuests;
    int price;
    int maxGuests;
    int extraPersonCharge;
    boolean reservationAvailable;

    public static RoomResponseDto toDto(Room room, boolean isAvailable) {
        return RoomResponseDto.builder()
                .id(room.getId())
                .name(room.getName())
                .baseGuests(room.getBaseGuests())
                .price(room.getPrice())
                .maxGuests(room.getMaxGuests())
                .extraPersonCharge(room.getExtraPersonCharge())
                .reservationAvailable(isAvailable)
                .build();
    }
}
