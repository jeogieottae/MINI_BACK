package com.example.mini.domain.cart.controller;

import com.example.mini.domain.cart.model.request.AddCartItemRequest;
import com.example.mini.domain.cart.model.request.ConfirmCartItemRequest;
import com.example.mini.domain.cart.model.request.DeleteCartItemRequest;
import com.example.mini.domain.cart.model.response.CartResponse;
import com.example.mini.domain.cart.service.CartService;
import com.example.mini.global.api.ApiResponse;
import com.example.mini.global.security.details.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@CrossOrigin(origins = {
    "http://localhost:8080",
    "http://3.38.1.70:8080",
    "http://ec2-3-38-1-70.ap-northeast-2.compute.amazonaws.com:8080",
    "http://localhost:3000",
    "https://localhost:3000",
    "https://127.0.0.1:3000"
})
public class CartController {

  private final CartService cartService;

  //장바구니 보기
  @GetMapping
  public ResponseEntity<ApiResponse<Page<CartResponse>>> getAllCartItems(
      @AuthenticationPrincipal UserDetailsImpl userDetails,
      @PageableDefault(size = 10) Pageable pageable
  ) {
    Page<CartResponse> cartItems = cartService.getAllCartItems(userDetails.getMemberId(), pageable);
    return ResponseEntity.ok(ApiResponse.OK(cartItems));
  }

  //장바구니 품목 추가
  @PostMapping
  public ResponseEntity addCartItem(
      @AuthenticationPrincipal UserDetailsImpl userDetails,
      @RequestBody AddCartItemRequest request
  ) {
    cartService.addCartItem(userDetails.getMemberId(), request);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  // 장바구니 품목 확정
  @PutMapping
  public ResponseEntity<Void> confirmCartItems(
      @AuthenticationPrincipal UserDetailsImpl userDetails,
      @RequestBody ConfirmCartItemRequest request
  ) {
    cartService.confirmCartItems(userDetails.getMemberId(), request);
    return ResponseEntity.ok().build();
  }

  //장바구니 품목 삭제
  @DeleteMapping
  public ResponseEntity<ApiResponse<Object>> deleteCartItem(
      @AuthenticationPrincipal UserDetailsImpl userDetails,
      @RequestBody DeleteCartItemRequest request
  ) {
    cartService.deleteCartItem(userDetails.getMemberId(), request);
    return ResponseEntity.ok(ApiResponse.DELETE());
  }
}