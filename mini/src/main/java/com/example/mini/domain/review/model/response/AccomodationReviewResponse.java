package com.example.mini.domain.review.model.response;

import java.time.LocalDateTime;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccomodationReviewResponse {
  private String comment;
  private int star;
  private String memberName;
  private LocalDateTime createdAt;

}
