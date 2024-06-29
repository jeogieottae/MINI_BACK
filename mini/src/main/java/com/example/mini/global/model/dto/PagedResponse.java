package com.example.mini.global.model.dto;

import lombok.*;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class PagedResponse<T> {
    private int totalPages;
    private Long totalElements;
    private List<T> content;
}
