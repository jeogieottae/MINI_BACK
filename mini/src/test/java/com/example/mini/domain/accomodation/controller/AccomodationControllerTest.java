/*
package com.example.mini.domain.accomodation.controller;

import com.example.mini.domain.accomodation.entity.enums.AccomodationCategory;
import com.example.mini.domain.accomodation.model.response.AccomodationCardResponseDto;
import com.example.mini.domain.accomodation.model.response.AccomodationDetailsResponseDto;
import com.example.mini.domain.accomodation.model.response.AccomodationResponseDto;
import com.example.mini.domain.accomodation.model.response.RoomResponseDto;
import com.example.mini.domain.accomodation.service.AccomodationService;
import com.example.mini.domain.review.model.response.ReviewResponse;
import com.example.mini.global.model.dto.PagedResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AccomodationController.class)
@MockBean(JpaMetamodelMappingContext.class)
public class AccomodationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccomodationService accomodationService;

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void 전체_숙소_목록조회_성공() throws Exception {
        List<AccomodationCardResponseDto> content = List.of(
                AccomodationCardResponseDto.builder()
                        .id(1L)
                        .name("숙소 이름 1")
                        .address("숙소 1 주소")
                        .checkIn(LocalDateTime.now())
                        .checkOut(LocalDateTime.now())
                        .category(AccomodationCategory.SEOUL.getName())
                        .build(),
                AccomodationCardResponseDto.builder()
                        .id(2L)
                        .name("숙소 이름 2")
                        .address("숙소 2 주소")
                        .checkIn(LocalDateTime.now())
                        .checkOut(LocalDateTime.now())
                        .category(AccomodationCategory.SEOUL.getName())
                        .build()
        );

        PagedResponse<AccomodationCardResponseDto> response = PagedResponse.<AccomodationCardResponseDto>builder()
                .content(content)
                .totalElements(2L)
                .totalPages(1)
                .build();

        when(accomodationService.getAllAccommodations(1)).thenReturn(response);

        mockMvc.perform(get("/api/accommodation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body.totalElements").value(2L))
                .andExpect(jsonPath("$.body.totalPages").value(1))
                .andExpect(jsonPath("$.body.content[0].name").value("숙소 이름 1"))
                .andExpect(jsonPath("$.body.content[1].name").value("숙소 이름 2"));

        verify(accomodationService).getAllAccommodations(1);
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void 숙소_이름으로_검색_성공() throws Exception {
        List<AccomodationCardResponseDto> content = List.of(
                AccomodationCardResponseDto.builder()
                        .id(1L)
                        .name("숙소 이름 1")
                        .address("숙소 1 주소")
                        .checkIn(LocalDateTime.now())
                        .checkOut(LocalDateTime.now())
                        .category(AccomodationCategory.SEOUL.getName())
                        .build(),
                AccomodationCardResponseDto.builder()
                        .id(2L)
                        .name("숙소 이름 2")
                        .address("숙소 2 주소")
                        .checkIn(LocalDateTime.now())
                        .checkOut(LocalDateTime.now())
                        .category(AccomodationCategory.SEOUL.getName())
                        .build()
        );

        PagedResponse<AccomodationCardResponseDto> response = PagedResponse.<AccomodationCardResponseDto>builder()
                .content(content)
                .totalElements(2L)
                .totalPages(1)
                .build();

        when(accomodationService.searchByAccommodationName("숙소", "", "", "", 1))
                .thenReturn(response);

        mockMvc.perform(get("/api/accommodation/search")
                .param("query", "숙소"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body.totalElements").value(2L))
                .andExpect(jsonPath("$.body.totalPages").value(1))
                .andExpect(jsonPath("$.body.content[0].name").value("숙소 이름 1"))
                .andExpect(jsonPath("$.body.content[1].name").value("숙소 이름 2"));

        verify(accomodationService).searchByAccommodationName(eq("숙소"), eq(""), eq(""), eq(""), anyInt());
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void 카테고리_이름으로_검색_성공() throws Exception {
        List<AccomodationCardResponseDto> content = List.of(
                AccomodationCardResponseDto.builder()
                        .id(1L)
                        .name("제주도 펜션")
                        .address("숙소 1 주소")
                        .checkIn(LocalDateTime.now())
                        .checkOut(LocalDateTime.now())
                        .category(AccomodationCategory.JEJU.getName())
                        .build(),
                AccomodationCardResponseDto.builder()
                        .id(2L)
                        .name("애월 호텔")
                        .address("숙소 2 주소")
                        .checkIn(LocalDateTime.now())
                        .checkOut(LocalDateTime.now())
                        .category(AccomodationCategory.JEJU.getName())
                        .build()
        );

        PagedResponse<AccomodationCardResponseDto> response = PagedResponse.<AccomodationCardResponseDto>builder()
                .content(content)
                .totalElements(2L)
                .totalPages(1)
                .build();

        when(accomodationService.searchByAccommodationName("", "제주", "", "", 1))
                .thenReturn(response);

        mockMvc.perform(get("/api/accommodation/search")
                .param("region", "제주"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body.totalElements").value(2L))
                .andExpect(jsonPath("$.body.totalPages").value(1))
                .andExpect(jsonPath("$.body.content[0].name").value("제주도 펜션"))
                .andExpect(jsonPath("$.body.content[1].name").value("애월 호텔"));

        verify(accomodationService).searchByAccommodationName(eq(""), eq("제주"), eq(""), eq(""), anyInt());
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void 숙소_상세정보_조회_성공() throws Exception {
        AccomodationResponseDto accomodation = AccomodationResponseDto.builder()
                .id(1L)
                .name("제주도 펜션")
                .address("숙소 주소 정보입니다.")
                .description("숙소 상세 정보입니다.")
                .category(AccomodationCategory.JEJU.getName())
                .build();
        List<RoomResponseDto> rooms = List.of(
                RoomResponseDto.builder()
                        .id(1L)
                        .name("제주도 펜션 객실 1")
                        .price(50000)
                        .baseGuests(2)
                        .reservationAvailable(true)
                        .extraPersonCharge(20000)
                        .build(),
                RoomResponseDto.builder()
                        .id(2L)
                        .name("제주도 펜션 객실 2")
                        .price(70000)
                        .baseGuests(2)
                        .reservationAvailable(true)
                        .extraPersonCharge(20000)
                        .build()
        );
        List<ReviewResponse> reviews = List.of(
                new ReviewResponse("좋아요!", 5),
                new ReviewResponse("뷰가 좋아요", 5)
        );
        Double avgStar = 5.0;

        AccomodationDetailsResponseDto response = AccomodationDetailsResponseDto.builder()
                .accomodation(accomodation)
                .rooms(rooms)
                .reviews(reviews)
                .avgStar(avgStar)
                .build();

        when(accomodationService.getAccomodationDetails(1L, "", ""))
                .thenReturn(response);

        mockMvc.perform(get("/api/accommodation/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body.accomodation.name").value("제주도 펜션"));

        verify(accomodationService).getAccomodationDetails(anyLong(), eq(""), eq(""));

    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void 객실_상세정보_조회_성공() throws Exception {
        RoomResponseDto room = RoomResponseDto.builder()
                .id(1L)
                .name("객실 이름")
                .baseGuests(2)
                .price(80000)
                .maxGuests(6)
                .reservationAvailable(true)
                .roomImageUrls(List.of("image url 1", "image url 2", "image url 3"))
                .build();

        when(accomodationService.getRoomDetail(1L, 1L, "", ""))
                .thenReturn(room);

        mockMvc.perform(get("/api/accommodation/1/room/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body.name").value("객실 이름"));

        verify(accomodationService).getRoomDetail(anyLong(), anyLong(), eq(""), eq(""));

    }

}
*/
