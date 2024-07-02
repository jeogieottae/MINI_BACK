package com.example.mini.domain.review.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.mini.domain.member.entity.Member;
import com.example.mini.domain.member.fixture.MemberEntityFixture;
import com.example.mini.domain.review.model.request.ReviewRequest;
import com.example.mini.domain.review.model.response.AccomodationReviewResponse;
import com.example.mini.domain.review.model.response.ReviewResponse;
import com.example.mini.domain.review.service.ReviewService;
import com.example.mini.global.api.exception.success.SuccessCode;
import com.example.mini.global.model.dto.PagedResponse;
import com.example.mini.global.security.details.UserDetailsImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Collections;

class ReviewControllerTest {

	@Mock
	private ReviewService reviewService;

	@InjectMocks
	private ReviewController reviewController;

	private MockMvc mockMvc;
	private ObjectMapper objectMapper;

	private Member member;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		this.mockMvc = MockMvcBuilders.standaloneSetup(reviewController).build();
		this.objectMapper = new ObjectMapper();
		this.member = MemberEntityFixture.getMember();
	}

	@Test
	void 리뷰_추가_성공() throws Exception {
		// Given
		ReviewRequest request = ReviewRequest.builder()
			.accomodationId(1L)
			.comment("좋았습니다!")
			.star(5)
			.build();

		ReviewResponse response = new ReviewResponse("좋았습니다!", 5);
		when(reviewService.addReview(any(Long.class), any(ReviewRequest.class))).thenReturn(response);

		// Mock Authentication
		UserDetailsImpl userDetails = new UserDetailsImpl(member);
		Authentication authentication = org.mockito.Mockito.mock(Authentication.class);
		when(authentication.getPrincipal()).thenReturn(userDetails);
		SecurityContext securityContext = org.mockito.Mockito.mock(SecurityContext.class);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);

		// When & Then
		mockMvc.perform(post("/api/reviews")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
				.principal(authentication))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.comment").value("좋았습니다!"))
			.andExpect(jsonPath("$.data.star").value(5))
			.andExpect(jsonPath("$.code").value(SuccessCode.REVIEW_ADDED.getHttpStatus().value()))
			.andExpect(jsonPath("$.message").value(SuccessCode.REVIEW_ADDED.getDescription()));
	}

	@Test
	void 숙소_리뷰_조회_성공() throws Exception {
		// Given
		AccomodationReviewResponse reviewResponse = new AccomodationReviewResponse("좋았습니다!", 5, "testname", LocalDateTime.now());
		PagedResponse<AccomodationReviewResponse> pagedResponse = new PagedResponse<>(1, 1L, Collections.singletonList(reviewResponse));
		when(reviewService.getReviewsByAccomodationId(1L, 1)).thenReturn(pagedResponse);

		// When & Then
		mockMvc.perform(get("/api/reviews")
				.param("id", "1")
				.param("page", "1"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.content[0].comment").value("좋았습니다!"))
			.andExpect(jsonPath("$.data.content[0].star").value(5))
			.andExpect(jsonPath("$.code").value(SuccessCode.REVIEWS_RETRIEVED.getHttpStatus().value()))
			.andExpect(jsonPath("$.message").value(SuccessCode.REVIEWS_RETRIEVED.getDescription()));
	}
}
