package com.example.mini.domain.review.model.request;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReviewRequest {
  private Long accomodationId;
  private String comment;
  private Integer star;
}
