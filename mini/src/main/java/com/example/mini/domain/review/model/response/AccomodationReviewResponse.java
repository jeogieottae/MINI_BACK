package com.example.mini.domain.review.model.response;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class AccomodationReviewResponse {
  private String comment;
  private int star;
  private String memberName;
  private LocalDateTime createdAt;

}
