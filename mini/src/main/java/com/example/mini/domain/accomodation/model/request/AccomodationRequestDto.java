package com.example.mini.domain.accomodation.model.request;

import com.example.mini.domain.accomodation.entity.Accomodation;
import lombok.*;

@Getter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class AccomodationRequestDto {

    private String name;
    private String description;
//    private int postalCode;
//    private String address;
//    private Boolean parkingAvailable;
//    private Boolean cookingAvailable;
//    private LocalDateTime checkIn;
//    private LocalDateTime checkOut;
//    private Long categoryId;

    public Accomodation toEntity() {
        return Accomodation.builder()
                .name(name)
                .description(description)
//                .postalCode(postalCode)
//                .address(address)
//                .parkingAvailable(parkingAvailable)
//                .cookingAvailable(cookingAvailable)
//                .checkIn(checkIn)
//                .checkOut(checkOut)
//                .category(category)
                .build();
    }
}
