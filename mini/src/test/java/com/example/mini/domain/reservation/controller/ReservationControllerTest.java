package com.example.mini.domain.reservation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import com.example.mini.domain.reservation.model.request.ReservationRequest;
import com.example.mini.domain.reservation.model.response.ReservationDetailResponse;
import com.example.mini.domain.reservation.model.response.ReservationResponse;
import com.example.mini.domain.reservation.model.response.ReservationSummaryResponse;
import com.example.mini.domain.reservation.service.ReservationService;
import com.example.mini.domain.member.entity.Member;
import com.example.mini.global.model.dto.PagedResponse;
import com.example.mini.global.security.details.UserDetailsImpl;
import com.example.mini.global.api.exception.success.SuccessCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
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
class ReservationControllerTest { /*모두 통과*/

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ReservationService reservationService;

	@Autowired
	private ObjectMapper objectMapper;

	private UserDetailsImpl getUserDetails() {
		Member member = Member.builder()
			.id(1L)
			.email("test@example.com")
			.nickname("testuser")
			.password("password")
			.name("Test User")
			.build();
		return new UserDetailsImpl(member);
	}

	@Test
	void 예약_확정_성공() throws Exception {
		UserDetailsImpl userDetails = getUserDetails();
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authentication);

		ReservationRequest request = new ReservationRequest();
		ReservationResponse response = ReservationResponse.builder()
			.roomId(1L)
			.accomodationName("Test Accomodation")
			.roomName("Test Room")
			.baseGuests(2)
			.maxGuests(4)
			.checkIn(LocalDateTime.now())
			.checkOut(LocalDateTime.now().plusDays(1))
			.peopleNumber(2)
			.totalPrice(100)
			.build();

		when(reservationService.createConfirmedReservation(any(Long.class), any(ReservationRequest.class))).thenReturn(response);

		mockMvc.perform(post("/api/reservation")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
				.with(csrf())
				.principal(authentication))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.result.resultCode").value(SuccessCode.RESERVATION_CONFIRMED.name()))
			.andExpect(jsonPath("$.result.resultMessage").value("success"))
			.andExpect(jsonPath("$.result.resultDescription").value(SuccessCode.RESERVATION_CONFIRMED.getDescription()))
			.andExpect(jsonPath("$.body.roomId").value(1L))
			.andExpect(jsonPath("$.body.accomodationName").value("Test Accomodation"))
			.andExpect(jsonPath("$.body.roomName").value("Test Room"));

		verify(reservationService).createConfirmedReservation(any(Long.class), any(ReservationRequest.class));
	}

	@Test
	void 모든_예약_조회_성공() throws Exception {
		UserDetailsImpl userDetails = getUserDetails();
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authentication);

		ReservationSummaryResponse summaryResponse = ReservationSummaryResponse.builder()
			.reservationId(1L)
			.accomodationName("Test Accomodation")
			.accomodationAddress("Test Address")
			.roomName("Test Room")
			.totalPrice(100)
			.peopleNumber(2)
			.checkIn(LocalDateTime.now())
			.checkOut(LocalDateTime.now().plusDays(1))
			.accomodationImageUrls(Collections.singletonList("test_image_url"))
			.build();
		PagedResponse<ReservationSummaryResponse> pagedResponse = new PagedResponse<>(1, 1L, Collections.singletonList(summaryResponse));

		when(reservationService.getAllReservations(any(Long.class), anyInt())).thenReturn(pagedResponse);

		mockMvc.perform(get("/api/reservation")
				.param("page", "1")
				.with(csrf())
				.principal(authentication))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.result.resultCode").value(SuccessCode.RESERVATIONS_RETRIEVED.name()))
			.andExpect(jsonPath("$.result.resultMessage").value("success"))
			.andExpect(jsonPath("$.result.resultDescription").value(SuccessCode.RESERVATIONS_RETRIEVED.getDescription()))
			.andExpect(jsonPath("$.body.totalElements").value(1L))
			.andExpect(jsonPath("$.body.totalPages").value(1))
			.andExpect(jsonPath("$.body.content[0].reservationId").value(1L))
			.andExpect(jsonPath("$.body.content[0].accomodationName").value("Test Accomodation"))
			.andExpect(jsonPath("$.body.content[0].roomName").value("Test Room"));

		verify(reservationService).getAllReservations(any(Long.class), anyInt());
	}

	@Test
	void 예약_상세정보_조회_성공() throws Exception {
		UserDetailsImpl userDetails = getUserDetails();
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authentication);

		ReservationDetailResponse detailResponse = ReservationDetailResponse.builder()
			.memberName("Test User")
			.accomodationName("Test Accomodation")
			.roomName("Test Room")
			.roomPrice(100)
			.baseGuests(2)
			.extraCharge(20)
			.checkIn(LocalDateTime.now())
			.checkOut(LocalDateTime.now().plusDays(1))
			.parkingAvailable(true)
			.cookingAvailable(true)
			.roomImageUrls(Collections.singletonList("test_room_image_url"))
			.build();

		when(reservationService.getReservationDetail(any(Long.class), any(Long.class))).thenReturn(detailResponse);

		mockMvc.perform(get("/api/reservation/detail/1")
				.with(csrf())
				.principal(authentication))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.result.resultCode").value(SuccessCode.RESERVATION_DETAIL_RETRIEVED.name()))
			.andExpect(jsonPath("$.result.resultMessage").value("success"))
			.andExpect(jsonPath("$.result.resultDescription").value(SuccessCode.RESERVATION_DETAIL_RETRIEVED.getDescription()))
			.andExpect(jsonPath("$.body.memberName").value("Test User"))
			.andExpect(jsonPath("$.body.accomodationName").value("Test Accomodation"))
			.andExpect(jsonPath("$.body.roomName").value("Test Room"));

		verify(reservationService).getReservationDetail(any(Long.class), any(Long.class));
	}
}
