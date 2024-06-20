package com.example.mini.domain.accomodation.model;

import com.example.mini.domain.accomodation.entity.Accomodation;
import com.example.mini.domain.accomodation.entity.Category;
import lombok.*;

import java.time.LocalDateTime;

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

    public Accomodation toEntity(Category category) {
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
