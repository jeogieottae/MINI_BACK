package com.example.mini.domain.cart.fixture;

import com.example.mini.domain.cart.entity.Cart;
import com.example.mini.domain.member.entity.Member;

public class CartEntityFixture {

	public static Cart getCart(Member member) {
		Cart cart = Cart.builder()
			.member(member)
			.build();
		return cart;
	}
}
