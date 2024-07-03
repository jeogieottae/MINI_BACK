package com.example.mini.domain.review.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.mini.domain.accomodation.entity.Accomodation;
import com.example.mini.domain.accomodation.repository.AccomodationRepository;
import com.example.mini.domain.member.entity.Member;
import com.example.mini.domain.member.repository.MemberRepository;
import com.example.mini.domain.reservation.entity.Reservation;
import com.example.mini.domain.reservation.entity.enums.ReservationStatus;
import com.example.mini.domain.reservation.repository.ReservationRepository;
import com.example.mini.global.security.config.SecurityConfig;
import com.example.mini.domain.review.model.request.ReviewRequest;
import com.example.mini.domain.review.model.response.AccomodationReviewResponse;
import com.example.mini.domain.review.model.response.ReviewResponse;
import com.example.mini.domain.review.service.ReviewService;
import com.example.mini.global.api.exception.success.SuccessCode;
import com.example.mini.global.model.dto.PagedResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;

@SpringBootTest
@AutoConfigureMockMvc
class ReviewControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ReviewService reviewService;

	@MockBean
	private MemberRepository memberRepository;

	@MockBean
	private AccomodationRepository accomodationRepository;

	@MockBean
	private ReservationRepository reservationRepository;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	@WithMockUser(username = "test@example.com", roles = "USER")
	void 리뷰_추가_성공() throws Exception {
		// Given
		ReviewRequest request = ReviewRequest.builder()
			.accomodationId(1L)
			.comment("좋아요")
			.star(5)
			.build();

		ReviewResponse response = new ReviewResponse("좋아요", 5);

		Member member = Member.builder()
			.id(1L)
			.email("test@example.com")
			.build();

		Accomodation accomodation = Accomodation.builder()
			.id(1L)
			.name("Test Accomodation")
			.build();

		Reservation reservation = Reservation.builder()
			.id(1L)
			.member(member)
			.accomodation(accomodation)
			.checkOut(LocalDateTime.now().minusDays(1))
			.status(ReservationStatus.CONFIRMED)
			.build();

		when(memberRepository.findById(any(Long.class))).thenReturn(Optional.of(member));
		when(accomodationRepository.findById(any(Long.class))).thenReturn(Optional.of(accomodation));
		when(reservationRepository.findByMemberIdAndAccomodationIdAndStatus(any(Long.class), any(Long.class), eq(ReservationStatus.CONFIRMED))).thenReturn(Optional.of(reservation));

		when(reviewService.addReview(any(Long.class), any(ReviewRequest.class))).thenReturn(response);

		// When & Then
		mockMvc.perform(post("/api/reviews")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andDo(result -> {
				String content = result.getResponse().getContentAsString();
				System.out.println("Response content: " + content);
			})
			.andExpect(jsonPath("$").isNotEmpty())
			.andExpect(jsonPath("$.result.resultCode").value(SuccessCode.REVIEW_ADDED.name()))
			.andExpect(jsonPath("$.result.resultMessage").value("success"))
			.andExpect(jsonPath("$.result.resultDescription").value(SuccessCode.REVIEW_ADDED.getDescription()))
			.andExpect(jsonPath("$.body.comment").value("좋아요"))
			.andExpect(jsonPath("$.body.star").value(5));

		verify(reviewService).addReview(eq(1L), any(ReviewRequest.class));
	}

	@Test
	@WithMockUser(username = "test@example.com", roles = "USER")
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
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$").isNotEmpty())
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
