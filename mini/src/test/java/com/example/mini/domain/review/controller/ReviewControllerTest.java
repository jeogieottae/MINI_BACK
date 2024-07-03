package com.example.mini.domain.review.controller;

import com.example.mini.domain.review.WithMockCustomUser;
import com.example.mini.domain.review.model.request.ReviewRequest;
import com.example.mini.domain.review.model.response.AccomodationReviewResponse;
import com.example.mini.domain.review.model.response.ReviewResponse;
import com.example.mini.domain.review.service.ReviewService;
import com.example.mini.global.api.ApiResponse;
import com.example.mini.global.api.exception.success.SuccessCode;
import com.example.mini.global.model.dto.PagedResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReviewControllerTest {

	@Mock
	private ReviewService reviewService;

	@InjectMocks
	private ReviewController reviewController;

	private MockMvc mockMvc;
	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		this.mockMvc = MockMvcBuilders.standaloneSetup(reviewController).build();
		this.objectMapper = new ObjectMapper();
	}

	@Test
	@WithMockCustomUser
	void 리뷰_추가_성공() throws Exception {
		// Given
		ReviewRequest request = ReviewRequest.builder()
			.accomodationId(1L)
			.comment("좋아요")
			.star(5)
			.build();

		ReviewResponse response = new ReviewResponse("좋아요", 5);
		when(reviewService.addReview(any(Long.class), any(ReviewRequest.class))).thenReturn(response);

		// When & Then
		mockMvc.perform(post("/api/reviews")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.result.resultCode").value(SuccessCode.REVIEW_ADDED.name()))
			.andExpect(jsonPath("$.result.resultMessage").value("success"))
			.andExpect(jsonPath("$.result.resultDescription").value(SuccessCode.REVIEW_ADDED.getDescription()))
			.andExpect(jsonPath("$.body.comment").value("좋아요"))
			.andExpect(jsonPath("$.body.star").value(5));
	}

	@Test
	@WithMockCustomUser
	void 숙소_리뷰_조회_성공() throws Exception {
		// Given
		AccomodationReviewResponse reviewResponse = AccomodationReviewResponse.builder()
			.comment("좋아요")
			.star(5)
			.memberName("하이")
			.createdAt(LocalDateTime.now())
			.build();

		PagedResponse<AccomodationReviewResponse> pagedResponse = new PagedResponse<>(1, 1L, Collections.singletonList(reviewResponse));
		when(reviewService.getReviewsByAccomodationId(1L, 1)).thenReturn(pagedResponse);

		// When & Then
		mockMvc.perform(get("/api/reviews")
				.param("id", "1")
				.param("page", "1"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.result.resultCode").value(SuccessCode.REVIEWS_RETRIEVED.name()))
			.andExpect(jsonPath("$.result.resultMessage").value("success"))
			.andExpect(jsonPath("$.result.resultDescription").value(SuccessCode.REVIEWS_RETRIEVED.getDescription()))
			.andExpect(jsonPath("$.body.totalPages").value(1))
			.andExpect(jsonPath("$.body.totalElements").value(1))
			.andExpect(jsonPath("$.body.content[0].comment").value("좋아요"))
			.andExpect(jsonPath("$.body.content[0].star").value(5))
			.andExpect(jsonPath("$.body.content[0].memberName").value("하이"))
			.andExpect(jsonPath("$.body.content[0].createdAt").exists());
	}
}
