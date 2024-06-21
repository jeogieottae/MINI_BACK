package com.example.mini.domain.cart.controller;

import com.example.mini.domain.cart.model.request.AddCartItemRequest;
import com.example.mini.domain.cart.model.request.DeleteCartItemRequest;
import com.example.mini.domain.cart.service.CartService;
import com.example.mini.global.api.ApiResponse;
import com.example.mini.global.security.details.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

  private final CartService cartService;

  //장바구니 보기
  @GetMapping("/items")
  public ResponseEntity getAllCartItems(
      @AuthenticationPrincipal UserDetailsImpl userDetails
  ) {
    return ResponseEntity.ok().body(cartService.getAllCartItems(userDetails.getMemberId()));
  }

  //장바구니 품목 추가
  @PostMapping("/items")
  public ResponseEntity addCartItem(
      @AuthenticationPrincipal UserDetailsImpl userDetails,
      @RequestBody AddCartItemRequest request
  ) {
    cartService.addCartItem(userDetails.getMemberId(), request);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }


  //장바구니 품목 삭제
  @DeleteMapping("/items")
  public ResponseEntity<ApiResponse<Object>> deleteCartItem(
      @AuthenticationPrincipal UserDetailsImpl userDetails,
      @RequestBody DeleteCartItemRequest request
  ) {
    cartService.deleteCartItem(userDetails.getMemberId(), request);
    return ResponseEntity.ok(ApiResponse.DELETE());
  }
}