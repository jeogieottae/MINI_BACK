package com.example.mini.domain.cart.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import com.example.mini.domain.cart.model.request.AddCartItemRequest;
import com.example.mini.domain.cart.model.request.ConfirmCartItemRequest;
import com.example.mini.domain.cart.model.request.DeleteCartItemRequest;
import com.example.mini.domain.cart.model.response.CartConfirmResponse;
import com.example.mini.domain.cart.model.response.CartResponse;
import com.example.mini.domain.cart.service.CartService;
import com.example.mini.domain.member.entity.Member;
import com.example.mini.global.model.dto.PagedResponse;
import com.example.mini.global.security.details.UserDetailsImpl;
import com.example.mini.global.api.exception.success.SuccessCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CartControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private CartService cartService;

	@Autowired
	private ObjectMapper objectMapper;

	private UserDetailsImpl getUserDetails() {
		Member member = Member.builder()
			.id(1L)
			.email("test@example.com")
			.nickname("testuser")
			.password("password")
			.build();
		return new UserDetailsImpl(member);
	}

	@Test
	void 장바구니_보기_성공() throws Exception {
		UserDetailsImpl userDetails = getUserDetails();
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authentication);

		PagedResponse<CartResponse> pagedResponse = new PagedResponse<>(1, 1L, Collections.singletonList(new CartResponse()));
		when(cartService.getAllCartItems(any(Long.class), anyInt())).thenReturn(pagedResponse);

		mockMvc.perform(get("/api/cart")
				.param("page", "1")
				.with(csrf())
				.principal(authentication))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.result.resultCode").value(SuccessCode.CART_ITEMS_RETRIEVED.name()))
			.andExpect(jsonPath("$.body.totalElements").value(1L))
			.andExpect(jsonPath("$.body.totalPages").value(1));

		verify(cartService).getAllCartItems(any(Long.class), anyInt());
	}

	@Test
	void 장바구니_품목_추가_성공() throws Exception {
		UserDetailsImpl userDetails = getUserDetails();
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authentication);

		AddCartItemRequest request = new AddCartItemRequest();

		mockMvc.perform(post("/api/cart")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
				.with(csrf())
				.principal(authentication))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.result.resultCode").value(SuccessCode.CART_ITEM_ADDED.name()))
			.andExpect(jsonPath("$.result.resultMessage").value("success"));

		verify(cartService).addCartItem(any(Long.class), any(AddCartItemRequest.class));
	}

	@Test
	void 장바구니_품목_확정_성공() throws Exception {
		UserDetailsImpl userDetails = getUserDetails();
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authentication);

		ConfirmCartItemRequest request = new ConfirmCartItemRequest();
		CartConfirmResponse response = new CartConfirmResponse();

		when(cartService.confirmReservationItem(any(Long.class), any(ConfirmCartItemRequest.class))).thenReturn(response);

		mockMvc.perform(put("/api/cart")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
				.with(csrf())
				.principal(authentication))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.result.resultCode").value(SuccessCode.CART_ITEM_CONFIRMED.name()))
			.andExpect(jsonPath("$.result.resultMessage").value("success"))
			.andExpect(jsonPath("$.body").isNotEmpty());

		verify(cartService).confirmReservationItem(any(Long.class), any(ConfirmCartItemRequest.class));
	}

	@Test
	void 장바구니_품목_삭제_성공() throws Exception {
		UserDetailsImpl userDetails = getUserDetails();
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authentication);

		DeleteCartItemRequest request = new DeleteCartItemRequest();

		mockMvc.perform(delete("/api/cart")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
				.with(csrf())
				.principal(authentication))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.result.resultCode").value(SuccessCode.CART_ITEM_DELETED.name()))
			.andExpect(jsonPath("$.result.resultMessage").value("success"));

		verify(cartService).deleteCartItem(any(Long.class), any(DeleteCartItemRequest.class));
	}
}
