package com.example.mini.domain.review.controller;

import com.example.mini.domain.review.model.request.ReviewRequest;
import com.example.mini.domain.review.model.response.AccomodationReviewResponse;
import com.example.mini.domain.review.model.response.ReviewResponse;
import com.example.mini.domain.review.service.ReviewService;
import com.example.mini.global.api.ApiResponse;
import com.example.mini.global.security.details.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
    Long memberId = userDetails.getMemberId();
    ReviewResponse response = reviewService.addReview(memberId, request);
    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }

  @GetMapping
  public ResponseEntity<ApiResponse<Page<AccomodationReviewResponse>>> getReviewsByAccomodationId(
      @RequestParam("accomodationId") Long accomodationId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size
  ) {
    Page<AccomodationReviewResponse> reviews = reviewService.getReviewsByAccomodationId(accomodationId, page, size);
    return ResponseEntity.ok(ApiResponse.OK(reviews));
  }

}
