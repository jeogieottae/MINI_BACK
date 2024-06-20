package com.example.mini.domain.cart.controller;

import com.example.mini.domain.cart.model.request.AddCartItemRequest;
import com.example.mini.domain.cart.model.request.DeleteCartItemRequest;
import com.example.mini.domain.cart.model.response.CartItemResponse;
import com.example.mini.domain.cart.model.response.CartResponse;
import com.example.mini.domain.cart.service.CartService;
import com.example.mini.global.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

  private final CartService cartService;

  @GetMapping("/{cartId}/items")
  public ResponseEntity<List<CartResponse>> getAllCartItems(@PathVariable Long cartId) {
    List<CartResponse> cartResponses = cartService.getAllCartItems();
    return ResponseEntity.ok((List<CartResponse>) ApiResponse.OK(cartResponses));
  }

  @PostMapping("/{cartId}/items")
  public ResponseEntity<ApiResponse<CartItemResponse>> addCartItem(@PathVariable Long cartId, @RequestBody AddCartItemRequest request) {
    CartItemResponse cartItemResponse = cartService.addCartItem(cartId, request);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.CREATED(cartItemResponse));
  }

  @DeleteMapping("/items")
  public ResponseEntity<ApiResponse<Object>> deleteCartItem(@RequestBody DeleteCartItemRequest request) {
    cartService.deleteCartItem(request);
    return ResponseEntity.ok(ApiResponse.DELETE());
  }
}