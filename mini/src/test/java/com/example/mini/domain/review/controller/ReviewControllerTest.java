package com.example.mini.domain.review.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.mini.domain.review.WithMockUserDetails;
import com.example.mini.domain.review.model.request.ReviewRequest;
import com.example.mini.domain.review.model.response.AccomodationReviewResponse;
import com.example.mini.domain.review.model.response.ReviewResponse;
import com.example.mini.domain.review.service.ReviewService;
import com.example.mini.global.api.exception.success.SuccessCode;
import com.example.mini.global.model.dto.PagedResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;

@WebMvcTest(ReviewController.class)
class ReviewControllerTest { /*리뷰 추가는 실패함*/

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ReviewService reviewService;

	@MockBean
	private JpaMetamodelMappingContext jpaMetamodelMappingContext;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	@WithMockUserDetails
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
	@WithMockUserDetails
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
