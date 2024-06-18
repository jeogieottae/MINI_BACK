package com.example.mini.domain.cart.service;

import com.example.mini.domain.accomodation.entity.Room;
import com.example.mini.domain.cart.entity.Cart;
import com.example.mini.domain.cart.entity.CartItem;
import com.example.mini.domain.cart.model.request.AddCartItemRequest;
import com.example.mini.domain.cart.model.request.DeleteCartItemRequest;
import com.example.mini.domain.cart.model.response.CartItemResponse;
import com.example.mini.domain.cart.repository.CartItemRepository;
import com.example.mini.domain.cart.repository.CartRepository;
import com.example.mini.domain.member.entity.Member;
import com.example.mini.domain.member.repository.MemberRepository;
import com.example.mini.domain.accomodation.repository.RoomRepository;
import com.example.mini.domain.cart.model.response.CartResponse;
import com.example.mini.global.exception.error.CartErrorCode;
import com.example.mini.global.exception.type.CartException;
import com.example.mini.global.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CartService {

  private final MemberRepository memberRepository;
  private final CartRepository cartRepository;
  private final CartItemRepository cartItemRepository;
  private final RoomRepository roomRepository;

  @Autowired
  public CartService(MemberRepository memberRepository, CartRepository cartRepository, CartItemRepository cartItemRepository, RoomRepository roomRepository) {
    this.memberRepository = memberRepository;
    this.cartRepository = cartRepository;
    this.cartItemRepository = cartItemRepository;
    this.roomRepository = roomRepository;
  }

  public List<CartResponse> getAllCartItems() {
    Long currentUserId = SecurityUtil.getCurrentUserId();
    Member member = memberRepository.findById(currentUserId)
        .orElseThrow(() -> new CartException(CartErrorCode.CART_NOT_FOUND));

    Cart cart = member.getCart();

    List<CartItem> cartItems = cart.getCartItem();

    List<CartResponse> cartResponses = cartItems.stream()
        .map(this::mapToCartResponse)
        .collect(Collectors.toList());

    return cartResponses;
  }

  private CartResponse mapToCartResponse(CartItem cartItem) {
    CartResponse cartResponse = new CartResponse();
    cartResponse.setId(cartItem.getId());
    cartResponse.setCheckIn(cartItem.getCheckIn());
    cartResponse.setCheckOut(cartItem.getCheckOut());
    cartResponse.setPeopleNumber(cartItem.getPeopleNumber());
    cartResponse.setPrice(cartItem.getPrice());
    return cartResponse;
  }

  public CartItemResponse addCartItem(Long cartId, AddCartItemRequest request) {
    Cart cart = cartRepository.findById(cartId)
        .orElseThrow(() -> new CartException(CartErrorCode.CART_NOT_FOUND));

    Room room = roomRepository.findById(request.getRoomId())
        .orElseThrow(() -> new CartException(CartErrorCode.ROOM_NOT_FOUND));

    CartItem cartItem = CartItem.builder()
        .checkIn(request.getCheckIn())
        .checkOut(request.getCheckOut())
        .peopleNumber(request.getPeopleNumber())
        .price(request.getPrice())
        .roomList(Collections.singletonList(room))
        .cart(cart)
        .build();

    cartItem = cartItemRepository.save(cartItem);

    CartItemResponse response = new CartItemResponse();
    response.setId(cartItem.getId());
    response.setCheckIn(cartItem.getCheckIn());
    response.setCheckOut(cartItem.getCheckOut());
    response.setPeopleNumber(cartItem.getPeopleNumber());
    response.setPrice(cartItem.getPrice());
    response.setRoomId(room.getId());

    return response;
  }

  public void deleteCartItem(DeleteCartItemRequest request) {
    List<Long> cartItemIds = request.getCartItemIds();
    cartItemIds.forEach(cartItemId -> {
      CartItem cartItem = cartItemRepository.findById(cartItemId)
          .orElseThrow(() -> new CartException(CartErrorCode.CART_NOT_FOUND));
      cartItemRepository.delete(cartItem);
    });
  }
}
