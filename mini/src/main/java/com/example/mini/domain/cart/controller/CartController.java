package com.example.mini.domain.cart.controller;

import com.example.mini.domain.cart.model.request.AddCartItemRequest;
import com.example.mini.domain.cart.model.request.DeleteCartItemRequest;
import com.example.mini.domain.cart.model.response.CartItemResponse;
import com.example.mini.domain.cart.model.response.CartResponse;
import com.example.mini.domain.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

  private final CartService cartService;

  @Autowired
  public CartController(CartService cartService) {
    this.cartService = cartService;
  }

  @GetMapping("/{cartId}/items")
  public List<CartResponse> getAllCartItems(@PathVariable Long cartId) {
    return cartService.getAllCartItems();
  }

  @PostMapping("/{cartId}/items")
  public CartItemResponse addCartItem(@PathVariable Long cartId, @RequestBody AddCartItemRequest request) {
    return cartService.addCartItem(cartId, request);
  }

  @DeleteMapping("/items")
  public void deleteCartItem(@RequestBody DeleteCartItemRequest request) {
    cartService.deleteCartItem(request);
  }
}
