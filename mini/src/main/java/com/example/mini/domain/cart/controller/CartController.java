package com.example.mini.domain.cart.controller;

import com.example.mini.domain.cart.model.request.AddCartItemRequest;
import com.example.mini.domain.cart.model.request.DeleteCartItemRequest;
import com.example.mini.domain.cart.model.response.CartItemResponse;
import com.example.mini.domain.cart.model.response.CartResponse;
import com.example.mini.domain.cart.service.CartService;
import com.example.mini.global.util.APIUtil;
import lombok.RequiredArgsConstructor;
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
    return APIUtil.OK(cartResponses);
  }

  @PostMapping("/{cartId}/items")
  public ResponseEntity<CartItemResponse> addCartItem(@PathVariable Long cartId, @RequestBody AddCartItemRequest request) {
    CartItemResponse cartItemResponse = cartService.addCartItem(cartId, request);
    return APIUtil.OK(cartItemResponse);
  }

  @DeleteMapping("/items")
  public ResponseEntity<Void> deleteCartItem(@RequestBody DeleteCartItemRequest request) {
    cartService.deleteCartItem(request);
    return APIUtil.OK();
  }
}