package com.example.mini.domain.review.model.response;

import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ReviewResponseDto {

  private String comment;
  private Integer star;
}