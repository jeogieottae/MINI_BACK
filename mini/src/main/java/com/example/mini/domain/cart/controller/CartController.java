package com.example.mini.domain.cart.controller;

import com.example.mini.domain.cart.model.request.AddCartItemRequest;
import com.example.mini.domain.cart.model.request.ConfirmCartItemRequest;
import com.example.mini.domain.cart.model.request.DeleteCartItemRequest;
import com.example.mini.domain.cart.model.response.CartConfirmResponse;
import com.example.mini.domain.cart.model.response.CartResponse;
import com.example.mini.domain.cart.service.CartService;
import com.example.mini.global.api.ApiResponse;
import com.example.mini.global.api.exception.success.SuccessCode;
import com.example.mini.global.model.dto.PagedResponse;
import com.example.mini.global.security.details.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

  private final CartService cartService;

  //장바구니 보기
  @GetMapping
  public ResponseEntity<ApiResponse<PagedResponse<CartResponse>>> getAllCartItems(
      @AuthenticationPrincipal UserDetailsImpl userDetails,
      @RequestParam(value = "page", defaultValue = "1") int page
  ) {
    PagedResponse<CartResponse> cartItems = cartService.getAllCartItems(userDetails.getMemberId(), page);
    return ResponseEntity.ok(ApiResponse.SUCCESS(SuccessCode.CART_ITEMS_RETRIEVED, cartItems));
  }

  //장바구니 품목 추가
  @PostMapping
  public ResponseEntity addCartItem(
      @AuthenticationPrincipal UserDetailsImpl userDetails,
      @RequestBody AddCartItemRequest request
  ) {
    cartService.addCartItem(userDetails.getMemberId(), request);
    return ResponseEntity.ok(ApiResponse.SUCCESS(SuccessCode.CART_ITEM_ADDED));
  }


  // 장바구니 품목 확정
  @PutMapping
  public ResponseEntity<ApiResponse<CartConfirmResponse>> confirmCartItems(
      @AuthenticationPrincipal UserDetailsImpl userDetails,
      @RequestBody ConfirmCartItemRequest request) {
    Long memberId = userDetails.getMemberId();
    CartConfirmResponse response = cartService.confirmReservationItem(memberId, request);
    return ResponseEntity.ok(ApiResponse.SUCCESS(SuccessCode.CART_ITEM_CONFIRMED, response));
  }

  //장바구니 품목 삭제
  @DeleteMapping
  public ResponseEntity<ApiResponse<Object>> deleteCartItem(
      @AuthenticationPrincipal UserDetailsImpl userDetails,
      @RequestBody DeleteCartItemRequest request
  ) {
    cartService.deleteCartItem(userDetails.getMemberId(), request);
    return ResponseEntity.ok(ApiResponse.SUCCESS(SuccessCode.CART_ITEM_DELETED));  }

}