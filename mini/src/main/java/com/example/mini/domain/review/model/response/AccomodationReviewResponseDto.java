package com.example.mini.domain.review.model.response;

import java.time.LocalDateTime;

import com.example.mini.domain.review.entity.Review;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccomodationReviewResponseDto {
  private String comment;
  private int star;
  private String memberName;
  private LocalDateTime createdAt;

  public static AccomodationReviewResponseDto toDto(Review review) {
    return AccomodationReviewResponseDto.builder()
            .comment(review.getComment())
            .star(review.getStar())
            .memberName(review.getMember().getName())
            .createdAt(review.getCreatedAt())
            .build();
  }
}
