package com.example.mini.domain.like.controller;

import com.example.mini.domain.like.model.response.AccomodationResponse;
import com.example.mini.domain.like.service.LikeService;
import com.example.mini.global.api.ApiResponse;
import com.example.mini.global.model.dto.PagedResponse;
import com.example.mini.global.security.details.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
public class LikeController {

  private final LikeService likeService;

  @PostMapping("{accomodationId}")
  public ResponseEntity addLike(
      @AuthenticationPrincipal UserDetailsImpl userDetails,
      @PathVariable Long accomodationId
  ) {
    likeService.addLike(userDetails.getMemberId(), accomodationId);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @GetMapping
  public ResponseEntity<ApiResponse<PagedResponse<AccomodationResponse>>> getLikedAccomodations(
      @AuthenticationPrincipal UserDetailsImpl userDetails,
      @RequestParam(value = "page", defaultValue = "1") int page
  ) {
    PagedResponse<AccomodationResponse> likedAccomodations = likeService.getLikedAccomodations(userDetails.getMemberId(), page);
    return ResponseEntity.ok(ApiResponse.OK(likedAccomodations));
  }
}
