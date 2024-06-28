package com.example.mini.domain.accomodation.service;


import org.springframework.data.domain.Pageable;
import com.example.mini.domain.accomodation.entity.Accomodation;
import com.example.mini.domain.accomodation.entity.Room;
import com.example.mini.domain.accomodation.entity.enums.AccomodationCategory;
import com.example.mini.domain.accomodation.model.request.AccomodationRequestDto;
import com.example.mini.domain.accomodation.model.AccomodationSearch;
import com.example.mini.domain.accomodation.model.response.AccomodationDetailsResponseDto;
import com.example.mini.domain.accomodation.model.response.AccomodationResponseDto;
import com.example.mini.domain.accomodation.model.response.PagedResponse;
import com.example.mini.domain.accomodation.model.response.RoomResponseDto;
import com.example.mini.domain.accomodation.repository.AccomodationRepository;
import com.example.mini.domain.accomodation.repository.RoomRepository;
import com.example.mini.domain.review.entity.Review;
import com.example.mini.domain.review.model.response.ReviewResponse;
import com.example.mini.domain.review.repository.ReviewRepository;
import com.example.mini.global.api.exception.GlobalException;
import com.example.mini.global.api.exception.error.AccomodationErrorCode;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AccomodationService {

    private final AccomodationRepository accomodationRepository;
    private final RoomRepository roomRepository;
    private final ReviewRepository reviewRepository;
    private final int PageSize = 5; // 페이지 크기

    /**
     * 전체 숙소 목록 조회
     *
     * @param page  조회할 페이지 번호
     * @return      숙소 정보 목록을 포함한 응답 객체
     */
    public PagedResponse<AccomodationResponseDto> getAllAccommodations(int page) {
        Page<Accomodation> accommodations = accomodationRepository.findAll(PageRequest.of(page-1, PageSize));
        checkPageException(accommodations);
        return setResponse(accommodations);
    }

    /**
     * 숙소 카테고리별 조회 (지역)
     *
     * @param categoryName  조회할 카테고리(지역) 이름
     * @param page          조회할 페이지 번호
     * @return              숙소 정보 목록을 포함한 응답 객체
     */
    public PagedResponse<AccomodationResponseDto> getAccommodationsByCategory(String categoryName, int page) {
        AccomodationCategory category = AccomodationCategory.fromName(categoryName);
        Page<Accomodation> accommodations = accomodationRepository.findByCategoryName(category, PageRequest.of(page-1, PageSize));
        checkPageException(accommodations);
        return setResponse(accommodations);
    }


    /**
     * 숙소 상세정보 조회
     *
     * @param accomodationId    숙소 id
     * @return                  숙소 정보 및 객실 목록을 포함한 응답 객체
     */
    public AccomodationDetailsResponseDto getAccomodationDetails(Long accomodationId) {
        Accomodation accomodation = accomodationRepository.findById(accomodationId)
                .orElseThrow(() -> new GlobalException(AccomodationErrorCode.RESOURCE_NOT_FOUND));
        List<Room> rooms = roomRepository.findByAccomodationId(accomodationId);

        Pageable pageable = PageRequest.of(0, 5);
        List<Review> latestReviews = reviewRepository.findTop5ByAccomodationOrderByCreatedAtDesc(accomodation, pageable);
        Double avgStar = reviewRepository.findAverageStarByAccomodation(accomodation);


        AccomodationResponseDto accomodationResponseDto = AccomodationResponseDto.toDto(accomodation);
        List<RoomResponseDto> roomResponseDtos = rooms.stream().map(RoomResponseDto::toDto).toList();

        List<ReviewResponse> reviewResponses = latestReviews.stream()
            .map(review -> {
                ReviewResponse response = new ReviewResponse();
                response.setComment(review.getComment());
                response.setStar(review.getStar());
                return response;
            })
            .collect(Collectors.toList());

        return AccomodationDetailsResponseDto.builder()
            .accomodation(accomodationResponseDto)
            .rooms(roomResponseDtos)
            .reviews(reviewResponses)
            .avgStar(avgStar)
            .build();
    }

    /**
     * 객실 상세정보 조회
     *
     * @param accomodationId    숙소 id
     * @param roomId            객실 id
     * @return                  객실 정보 객체
     */
    public RoomResponseDto getRoomDetail(Long accomodationId, Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new GlobalException(AccomodationErrorCode.RESOURCE_NOT_FOUND));
        if(!accomodationId.equals(room.getAccomodation().getId()))
            throw new GlobalException(AccomodationErrorCode.INVALID_ROOM_REQUEST);
        return RoomResponseDto.toDto(room);
    }

    /**
     * Entity → Dto 변환 및 응답 객체로 변환하는 메서드
     *
     * @param accommodations    변환할 객체
     * @return                  숙소 정보 목록을 포함한 응답 객체
     */
    private PagedResponse<AccomodationResponseDto> setResponse(Page<Accomodation> accommodations) {
        List<AccomodationResponseDto> content = accommodations.getContent().stream()
                .map(AccomodationResponseDto::toDto)
                .toList();
        return new PagedResponse<>(accommodations.getTotalPages(), accommodations.getTotalElements(), content);
    }

    /**
     * 페이지네이션 공통 에러처리
     *
     * @param accommodations    검증할 객체
     */
    private void checkPageException(Page<Accomodation> accommodations) {
        if (accommodations.getContent().isEmpty() || accommodations.isEmpty()) {
            throw new GlobalException(AccomodationErrorCode.RESOURCE_NOT_FOUND);
        }
    }

}
