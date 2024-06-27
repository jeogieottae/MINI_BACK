package com.example.mini.domain.review.model.request;

import lombok.Data;

@Data
public class ReviewRequest {
  private Long accomodationId;
  private String comment;
  private Integer star;
}
