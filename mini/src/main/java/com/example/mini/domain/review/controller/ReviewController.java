package com.example.mini.domain.review.controller;

import com.example.mini.domain.review.model.request.ReviewRequestDto;
import com.example.mini.domain.review.model.response.AccomodationReviewResponseDto;
import com.example.mini.domain.review.model.response.ReviewResponseDto;
import com.example.mini.domain.review.service.ReviewService;
import com.example.mini.global.api.ApiResponse;
import com.example.mini.global.api.exception.success.SuccessCode;
import com.example.mini.global.model.dto.PagedResponse;
import com.example.mini.global.security.details.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

  private final ReviewService reviewService;

  @PostMapping
  public ResponseEntity<ApiResponse<ReviewResponseDto>> addReview(
      @RequestBody ReviewRequestDto request,
      @AuthenticationPrincipal UserDetailsImpl userDetails
  ) {
    ReviewResponseDto response = reviewService.addReview(userDetails.getMemberId(), request);
    return ResponseEntity.ok(ApiResponse.SUCCESS(SuccessCode.REVIEW_ADDED, response));
  }

  @GetMapping
  public ResponseEntity<ApiResponse<PagedResponse<AccomodationReviewResponseDto>>> getReviewsByAccomodationId(
      @RequestParam Long accommodationId,
      @RequestParam(defaultValue = "1") int page
  ) {
    PagedResponse<AccomodationReviewResponseDto> reviews = reviewService.getReviewsByAccomodationId(accommodationId, page);
    return ResponseEntity.ok(ApiResponse.SUCCESS(SuccessCode.REVIEWS_RETRIEVED, reviews));
  }
}