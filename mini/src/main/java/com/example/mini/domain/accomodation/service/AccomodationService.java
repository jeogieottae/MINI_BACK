package com.example.mini.domain.accomodation.service;


import com.example.mini.domain.accomodation.model.response.*;
import com.example.mini.domain.reservation.entity.Reservation;
import com.example.mini.domain.reservation.repository.ReservationRepository;
import com.example.mini.global.model.dto.PagedResponse;
import com.example.mini.global.util.datetime.DateTimeUtil;
import com.example.mini.domain.accomodation.entity.Accomodation;
import com.example.mini.domain.accomodation.entity.Room;
import com.example.mini.domain.accomodation.entity.enums.AccomodationCategory;
import com.example.mini.domain.accomodation.model.response.AccomodationSearch;
import com.example.mini.domain.accomodation.repository.AccomodationRepository;
import com.example.mini.domain.accomodation.repository.AccomodationSearchRepository;
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

import static java.util.Arrays.asList;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AccomodationService {

    private final AccomodationRepository accomodationRepository;
    private final AccomodationSearchRepository accomodationSearchRepository;
    private final RoomRepository roomRepository;
    private final ReviewRepository reviewRepository;
    private final ReservationRepository reservationRepository;
    private final int PageSize = 20;        // 숙소 목록 페이지 크기

    /**
     * 전체 숙소 목록 조회
     *
     * @param page  조회할 페이지 번호
     * @return      숙소 정보 목록을 포함한 응답 객체
     */
    public PagedResponse<AccomodationCardResponseDto> getAllAccommodations(int page) {
        Page<Accomodation> accommodations = accomodationRepository.findAll(PageRequest.of(page-1, PageSize));
        checkPageException(accommodations);
        return setResponse(accommodations, null, null);
    }

    /**
     * 숙소 카테고리별 조회 (지역)
     *
     * @param categoryName  조회할 카테고리(지역) 이름
     * @param page          조회할 페이지 번호
     * @return              숙소 정보 목록을 포함한 응답 객체
     */
    public PagedResponse<AccomodationCardResponseDto> getAccommodationsByCategory(String categoryName, int page, String checkIn, String checkOut) {
        AccomodationCategory category = AccomodationCategory.fromName(categoryName);
        Page<Accomodation> accommodations = accomodationRepository.findByCategoryName(category, PageRequest.of(page-1, PageSize));
        checkPageException(accommodations);
        return setResponse(accommodations, checkIn, checkOut);
    }

    /**
     * 숙소 이름으로 검색
     *
     * @param keyword   검색 키워드
     * @return          숙소 정보 목록을 포함한 응답 객체
     */
    public PagedResponse<AccomodationCardResponseDto> searchByAccommodationName(String keyword, int page, String checkIn, String checkOut) {
        List<AccomodationSearch> searches = accomodationSearchRepository.findAccommodationsByName(keyword);
        List<Long> idList = searches.stream().map(AccomodationSearch::getId).toList();
        Page<Accomodation> accommodations = accomodationRepository.findByIdList(idList, PageRequest.of(page-1, PageSize));
        checkPageException(accommodations);
        return setResponse(accommodations, checkIn, checkOut);
    }

    /**
     * 숙소 상세정보 조회
     *
     * @param accomodationId    숙소 id
     * @return                  숙소 정보 및 객실 목록을 포함한 응답 객체
     */
    public AccomodationDetailsResponseDto getAccomodationDetails(Long accomodationId, String checkIn, String checkOut) {
        Accomodation accomodation = accomodationRepository.findById(accomodationId)
            .orElseThrow(() -> new GlobalException(AccomodationErrorCode.RESOURCE_NOT_FOUND));

        AccomodationResponseDto accomodationResponseDto = AccomodationResponseDto.toDto(accomodation);
        Double avgStar = reviewRepository.findAverageStarByAccomodation(accomodation);
        List<ReviewResponse> reviewResponses = getReviewResponse(accomodation);
        List<RoomResponseDto> roomResponseDtos = getRoomResponseDto(accomodationId, checkIn, checkOut);

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
        return RoomDetailResponseDto.toDto(room);
    }

    /**
     * Entity → Dto 변환 및 응답 객체로 변환하는 메서드
     *
     * @param accommodations    변환할 객체
     * @return                  숙소 정보 목록을 포함한 응답 객체
     */
    private PagedResponse<AccomodationCardResponseDto> setResponse(
        Page<Accomodation> accommodations,
        String checkIn,
        String checkOut
    ) {
        List<AccomodationCardResponseDto> content = accommodations.getContent().stream().map(accommodation -> {
            Integer minPrice = roomRepository.findMinPriceByAccommodationId(accommodation.getId());
            List<Room> rooms = roomRepository.findByAccomodationId(accommodation.getId());
            List<Boolean> availables = rooms.stream().map(room -> reservationAvailable(checkIn, checkOut, room.getId())).toList();
            boolean isAvailable = checkAllReservationAvailable(availables);
            return AccomodationCardResponseDto.toDto(accommodation, minPrice, isAvailable);
        }).toList();

        return new PagedResponse<>(accommodations.getTotalPages(), accommodations.getTotalElements(), content);
    }

    /**
     * 해당 객실의 예약가능 여부를 반환하는 메서드
     * @param checkIn   예약할 checkIn 정보
     * @param checkOut  예약할 checkOut 정보
     * @param roomId    조회할 객실 id
     * @return          예약가능 여부
     */
    private boolean reservationAvailable(String checkIn, String checkOut, Long roomId) {
        List<Long> list = asList(roomId);
        List<LocalDateTime> checkInOut = DateTimeUtil.parseDateTimes(checkIn, checkOut);
        List<Reservation> reservations = reservationRepository.findOverlappingReservations(list, checkInOut.get(0), checkInOut.get(1));
        return reservations.isEmpty();
    }

    /**
     * 해당 숙소의 전 객실에 대한 예약가능 여부를 반환하는 메서드
     * @param availables    각 객실의 예약가능 여부
     * @return              숙소 예약가능 여부 (true: 예약 가능)
     */
    private boolean checkAllReservationAvailable(List<Boolean> availables) {
        return availables.stream().anyMatch(available -> available);
    }

    /**
     * 페이지네이션 공통 에러처리 메서드
     * @param accommodations    검증할 객체
     */
    private void checkPageException(Page<Accomodation> accommodations) {
        if (accommodations.getContent().isEmpty() || accommodations.isEmpty()) {
            throw new GlobalException(AccomodationErrorCode.RESOURCE_NOT_FOUND);
        }
    }

    /**
     * 숙소 상세정보의 객실 데이터 반환 메서드
     * @param accomodationId    해당 숙소의 id
     * @param checkIn           체크인 시간 ( default: 당일 ~ 익일 )
     * @param checkOut          체크아웃 시간
     * @return                  객실 정보가 담긴 객체 리스트 반환
     */
    private List<RoomResponseDto> getRoomResponseDto(Long accomodationId, String checkIn, String checkOut) {
        List<Room> rooms = roomRepository.findByAccomodationId(accomodationId);
        return  rooms.stream().map(room -> {
            Boolean isAvailable = reservationAvailable(checkIn, checkOut, room.getId());
            RoomResponseDto dto = RoomResponseDto.toDto(room, isAvailable);
            return dto;
        }).toList();
    }


    /**
     * 해당 숙소의 최근 작성된 리뷰 5개를 반환하는 메서드
     * @param accomodation  조회할 숙소 정보
     * @return              최근 작성된 리뷰 객체 리스트 반환
     */
    private List<ReviewResponse> getReviewResponse(Accomodation accomodation) {
        System.out.println(accomodation.toString());
        List<Review> latestReviews = reviewRepository.findTop5ByAccomodationOrderByCreatedAtDesc(accomodation, PageRequest.of(0, 5));
        return latestReviews.stream()
            .map(review -> new ReviewResponse(review.getComment(), review.getStar())).collect(Collectors.toList());
    }

}