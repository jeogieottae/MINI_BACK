package com.example.mini.domain.accomodation.service;


import com.example.mini.domain.accomodation.model.response.*;
import com.example.mini.domain.reservation.entity.Reservation;
import com.example.mini.domain.reservation.repository.ReservationRepository;
import org.springframework.data.domain.Pageable;
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

import java.time.format.DateTimeFormatter;
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
    private final int PageSize = 20; // 페이지 크기


    /**
     * 전체 숙소 목록 조회
     *
     * @param page  조회할 페이지 번호
     * @return      숙소 정보 목록을 포함한 응답 객체
     */
    public PagedResponse<AccomodationCardResponseDto> getAllAccommodations(int page) {
        Page<Accomodation> accommodations = accomodationRepository.findAll(PageRequest.of(page-1, PageSize));
        checkPageException(accommodations);
        return setCardResponse(accommodations);
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
    public AccomodationDetailsResponseDto getAccomodationDetails(Long accomodationId) {
        Accomodation accomodation = accomodationRepository.findById(accomodationId)
                .orElseThrow(() -> new GlobalException(AccomodationErrorCode.RESOURCE_NOT_FOUND));
        List<Room> rooms = roomRepository.findByAccomodationId(accomodationId);
        AccomodationResponseDto accomodationResponseDto = AccomodationResponseDto.toDto(accomodation);

        Pageable pageable = PageRequest.of(0, 5);
        List<Review> latestReviews = reviewRepository.findTop5ByAccomodationOrderByCreatedAtDesc(accomodation, pageable);
        Double avgStar = reviewRepository.findAverageStarByAccomodation(accomodation);
        LocalDateTime checkIn = LocalDateTime.now();
        LocalDateTime checkOut = LocalDateTime.now();

        List<RoomResponseDto> roomResponseDtos = rooms.stream().map(room -> {
            Boolean isAvailable = reservationAvailable(checkIn, checkOut, room.getId());
            RoomResponseDto dto = RoomResponseDto.toDto(room, isAvailable);
            return dto;
        }).toList();

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
        List<LocalDateTime> checkInOut = dateTimeFormatter(checkIn, checkOut);
        List<AccomodationCardResponseDto> content = accommodations.getContent().stream()
                .map(accommodation -> {
                    Integer minPrice = roomRepository.findMinPriceByAccommodationId(accommodation.getId());
                    List<Room> rooms = roomRepository.findByAccomodationId(accommodation.getId());

                    List<Boolean> availables = rooms.stream().map(room -> {
                        return reservationAvailable(checkInOut.get(0), checkInOut.get(1), room.getId());
                    }).toList();
                    boolean isAvailable = checkAllReservationAvailable(availables);
                    return AccomodationCardResponseDto.toDto(accommodation, minPrice, isAvailable);
                }).toList();

        return new PagedResponse<>(accommodations.getTotalPages(), accommodations.getTotalElements(), content);
    }

    private PagedResponse<AccomodationCardResponseDto> setCardResponse(Page<Accomodation> accommodations) {
        List<AccomodationCardResponseDto> content = accommodations.getContent().stream()
                .map(accommodation -> {
                    Integer minPrice = roomRepository.findMinPriceByAccommodationId(accommodation.getId());
                    List<Room> rooms = roomRepository.findByAccomodationId(accommodation.getId());
                    LocalDateTime checkIn = LocalDateTime.now();
                    LocalDateTime checkOut = checkIn.plusDays(1);

                    List<Boolean> availables = rooms.stream().map(room -> {
                        return reservationAvailable(checkIn, checkOut, room.getId());
                    }).toList();
                    boolean isAvailable = checkAllReservationAvailable(availables);
                    return AccomodationCardResponseDto.toDto(accommodation, minPrice, isAvailable);
                })
                .toList();
        return new PagedResponse<>(accommodations.getTotalPages(), accommodations.getTotalElements(), content);
    }

    /**
     * 해당 객실의 예약가능 여부를 반환하는 메서드
     *
     * @param checkIn   예약할 checkIn 정보
     * @param checkOut  예약할 checkOut 정보
     * @param roomId    조회할 객실 id
     * @return          예약가능 여부
     */
    private boolean reservationAvailable(LocalDateTime checkIn, LocalDateTime checkOut, Long roomId) {
        List<Long> list = asList(roomId);
        List<Reservation> reservations = reservationRepository.findOverlappingReservations(list, checkIn, checkOut);
        return reservations.isEmpty();
    }

    private boolean checkAllReservationAvailable(List<Boolean> availables) {
        return availables.stream().anyMatch(available -> available);
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

    /**
     * 체크인 체크아웃 형식 변환
     *
     * @param checkIn   체크인
     * @param checkOut  체크아웃
     * @return          체크인 체크아웃 리스트
     */
    private List<LocalDateTime> dateTimeFormatter(String checkIn, String checkOut) {
        LocalDateTime ConvertedCheckIn;
        LocalDateTime ConvertedCheckOut;
        if (checkIn.isEmpty()) {
            ConvertedCheckIn = LocalDateTime.now();
            ConvertedCheckOut = ConvertedCheckIn.minusDays(1);
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            ConvertedCheckIn = LocalDateTime.parse(checkIn, formatter);
            ConvertedCheckOut = LocalDateTime.parse(checkOut, formatter);
        }
        return asList(ConvertedCheckIn, ConvertedCheckOut);
    }
}
