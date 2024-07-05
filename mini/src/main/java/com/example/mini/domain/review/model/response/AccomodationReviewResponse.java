package com.example.mini.domain.review.model.response;

import java.time.LocalDateTime;

import com.example.mini.domain.review.entity.Review;
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

  public static AccomodationReviewResponse toDto(Review review) {
    return AccomodationReviewResponse.builder()
            .comment(review.getComment())
            .star(review.getStar())
            .memberName(review.getMember().getName())
            .createdAt(review.getCreatedAt())
            .build();
  }
}
