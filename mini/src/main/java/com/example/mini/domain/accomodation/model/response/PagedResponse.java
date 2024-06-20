package com.example.mini.domain.accomodation.model.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponse<T> {
    private int totalPages;
    private Long totalElements;
    private List<T> content;
}
