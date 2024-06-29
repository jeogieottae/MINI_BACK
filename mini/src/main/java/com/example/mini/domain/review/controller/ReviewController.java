package com.example.mini.domain.review.controller;

import com.example.mini.domain.review.model.request.ReviewRequest;
import com.example.mini.domain.review.model.response.AccomodationReviewResponse;
import com.example.mini.domain.review.model.response.ReviewResponse;
import com.example.mini.domain.review.service.ReviewService;
import com.example.mini.global.api.ApiResponse;
import com.example.mini.global.model.dto.PagedResponse;
import com.example.mini.global.security.details.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

  private final ReviewService reviewService;

  @PostMapping
  public ResponseEntity<ReviewResponse> addReview(
      @RequestBody ReviewRequest request,
      @AuthenticationPrincipal UserDetailsImpl userDetails
  ) {
    ReviewResponse response = reviewService.addReview(userDetails.getMemberId(), request);
    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }

  @GetMapping
  public ResponseEntity<ApiResponse<PagedResponse<AccomodationReviewResponse>>> getReviewsByAccomodationId(
      @RequestParam(value = "id", required = true) Long accommodationId,
      @RequestParam(defaultValue = "1") int page
  ) {
    PagedResponse<AccomodationReviewResponse> reviews = reviewService.getReviewsByAccomodationId(accommodationId, page);
    return ResponseEntity.ok(ApiResponse.OK(reviews));
  }

}