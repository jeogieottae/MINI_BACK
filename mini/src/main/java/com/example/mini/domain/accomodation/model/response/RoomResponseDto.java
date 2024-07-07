package com.example.mini.domain.accomodation.model.response;

import com.example.mini.domain.accomodation.entity.Room;
import com.example.mini.domain.accomodation.entity.image.RoomImage;
import java.util.List;
import java.util.stream.Collectors;
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
    private List<String> roomImageUrls;

    public static RoomResponseDto toDto(Room room, boolean isAvailable) {
        List<String> roomImageUrls = room.getImages().stream()
            .map(RoomImage::getImgUrl)
            .collect(Collectors.toList());

        return RoomResponseDto.builder()
            .id(room.getId())
            .name(room.getName())
            .baseGuests(room.getBaseGuests())
            .price(room.getPrice())
            .maxGuests(room.getMaxGuests())
            .extraPersonCharge(room.getExtraPersonCharge())
            .reservationAvailable(isAvailable)
            .roomImageUrls(roomImageUrls)
            .build();
    }
}