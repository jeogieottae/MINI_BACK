package com.example.mini.domain.like.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import com.example.mini.domain.accomodation.entity.Accomodation;
import com.example.mini.domain.accomodation.entity.image.AccomodationImage;
import com.example.mini.domain.like.service.LikeService;
import com.example.mini.domain.member.entity.Member;
import com.example.mini.domain.member.repository.MemberRepository;
import com.example.mini.global.model.dto.PagedResponse;
import com.example.mini.global.security.details.UserDetailsImpl;
import com.example.mini.domain.like.model.response.AccomodationResponse;
import com.example.mini.global.api.exception.success.SuccessCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
class LikeControllerTest { /*모두 통과*/

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private LikeService likeService;

	@MockBean
	private MemberRepository memberRepository;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void 좋아요_토글_성공() throws Exception {
		// Given
		boolean isLiked = true;

		Member member = Member.builder()
			.id(1L)
			.email("test@example.com")
			.nickname("testuser")
			.password("password")
			.build();

		Accomodation.builder()
			.id(1L)
			.name("Test Accomodation")
			.build();

		when(memberRepository.findById(any(Long.class))).thenReturn(Optional.of(member));
		when(likeService.toggleLike(any(Long.class), any(Long.class))).thenReturn(isLiked);

		// Mocking UserDetailsImpl
		UserDetailsImpl userDetails = new UserDetailsImpl(member);
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authentication);

		// When & Then
		mockMvc.perform(post("/api/likes/1")
				.with(csrf())
				.principal(authentication))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andDo(result -> {
				String content = result.getResponse().getContentAsString();
				System.out.println("Response content: " + content);
			})
			.andExpect(jsonPath("$").isNotEmpty())
			.andExpect(jsonPath("$.result.resultCode").value(SuccessCode.LIKE_TOGGLED.name()))
			.andExpect(jsonPath("$.result.resultMessage").value("success"))
			.andExpect(jsonPath("$.result.resultDescription").value(SuccessCode.LIKE_TOGGLED.getDescription()))
			.andExpect(jsonPath("$.body").value(isLiked));

		verify(likeService).toggleLike(eq(1L), any(Long.class));
	}

	@Test
	void 좋아요_숙소_조회_성공() throws Exception {
		// Given
		Accomodation accomodation = Accomodation.builder()
			.id(1L)
			.name("좋아요한 숙소")
			.description("좋아요한 숙소 설명")
			.postalCode("12345")
			.address("서울시 강남구")
			.images(List.of(AccomodationImage.builder().imgUrl("testImage.jpg").build()))
			.build();

		AccomodationResponse accomodationResponse = AccomodationResponse.toDto(accomodation);

		PagedResponse<AccomodationResponse> pagedResponse = new PagedResponse<>(1, 1L, Collections.singletonList(accomodationResponse));
		when(likeService.getLikedAccomodations(any(Long.class), anyInt())).thenReturn(pagedResponse);

		Member member = Member.builder()
			.id(1L)
			.email("test@example.com")
			.nickname("testuser")
			.password("password")
			.build();

		// Mocking UserDetailsImpl
		UserDetailsImpl userDetails = new UserDetailsImpl(member);
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authentication);

		// When & Then
		mockMvc.perform(get("/api/likes")
				.param("page", "1")
				.with(csrf())
				.principal(authentication))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$").isNotEmpty())
			.andExpect(jsonPath("$.result.resultCode").value(SuccessCode.LIKED_ACCOMMODATIONS_RETRIEVED.name()))
			.andExpect(jsonPath("$.result.resultMessage").value("success"))
			.andExpect(jsonPath("$.result.resultDescription").value(SuccessCode.LIKED_ACCOMMODATIONS_RETRIEVED.getDescription()))
			.andExpect(jsonPath("$.body.totalElements").value(1L))
			.andExpect(jsonPath("$.body.totalPages").value(1))
			.andExpect(jsonPath("$.body.content[0].name").value("좋아요한 숙소"))
			.andExpect(jsonPath("$.body.content[0].description").value("좋아요한 숙소 설명"))
			.andExpect(jsonPath("$.body.content[0].postalCode").value("12345"))
			.andExpect(jsonPath("$.body.content[0].address").value("서울시 강남구"));

		verify(likeService).getLikedAccomodations(any(Long.class), anyInt());
	}
}
