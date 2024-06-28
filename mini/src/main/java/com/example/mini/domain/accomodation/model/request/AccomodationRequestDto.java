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

    public Accomodation toEntity() {
        return Accomodation.builder()
                .name(name)
                .description(description)
                .build();
    }
}
