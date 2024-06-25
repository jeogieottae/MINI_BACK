package com.example.mini.domain.accomodation.model.response;

import lombok.*;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponse<T> {
    private int totalPages;
    private Long totalElements;
    private List<T> content;
}
