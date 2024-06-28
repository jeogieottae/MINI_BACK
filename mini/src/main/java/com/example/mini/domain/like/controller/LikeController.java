package com.example.mini.domain.like.controller;

import com.example.mini.domain.like.model.request.AccomodationLikeRequest;
import com.example.mini.domain.like.model.response.AccomodationResponse;
import com.example.mini.domain.like.service.LikeService;
import com.example.mini.global.api.ApiResponse;
import com.example.mini.global.security.details.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
public class LikeController {

  private final LikeService likeService;

  @PostMapping
  public ResponseEntity addLike(
      @AuthenticationPrincipal UserDetailsImpl userDetails,
      @RequestBody AccomodationLikeRequest request) {
    likeService.addLike(userDetails.getMemberId(), request.getAccomodationId());
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @GetMapping
  public ResponseEntity<ApiResponse<Page<AccomodationResponse>>> getLikedAccomodations(
      @AuthenticationPrincipal UserDetailsImpl userDetails,
      @PageableDefault(size = 10) Pageable pageable
  ) {
    Page<AccomodationResponse> likedAccomodations = likeService.getLikedAccomodations(userDetails.getMemberId(), pageable);
    return ResponseEntity.ok(ApiResponse.OK(likedAccomodations));
  }
}
