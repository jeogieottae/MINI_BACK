package com.example.mini.domain.accomodation.service;

import com.example.mini.domain.accomodation.converter.AccomodationConverter;
import com.example.mini.domain.accomodation.entity.Accomodation;
import com.example.mini.domain.accomodation.entity.Room;
import com.example.mini.domain.accomodation.model.request.AccommodationRequestDto;
import com.example.mini.domain.accomodation.model.response.AccomodationCardResponseDto;
import com.example.mini.domain.accomodation.model.response.AccomodationDetailsResponseDto;
import com.example.mini.domain.accomodation.model.response.RoomResponseDto;
import com.example.mini.domain.accomodation.repository.AccomodationRepository;
import com.example.mini.domain.accomodation.repository.AccomodationSearchRepository;
import com.example.mini.domain.accomodation.repository.RoomRepository;
import com.example.mini.domain.accomodation.util.AccommodationUtils;
import com.example.mini.domain.like.entity.Like;
import com.example.mini.domain.like.repository.LikeRepository;
import com.example.mini.domain.reservation.entity.Reservation;
import com.example.mini.domain.reservation.repository.ReservationRepository;
import com.example.mini.domain.review.entity.Review;
import com.example.mini.domain.review.model.response.ReviewResponseDto;
import com.example.mini.global.api.exception.GlobalException;
import com.example.mini.global.api.exception.error.AccomodationErrorCode;
import com.example.mini.global.model.dto.PagedResponse;
import com.example.mini.global.util.datetime.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static com.example.mini.domain.accomodation.util.AccommodationUtils.checkPageException;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AccomodationService {

    private final AccomodationRepository accomodationRepository;
    private final AccomodationSearchRepository accomodationSearchRepository;
    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;
    private final LikeRepository likeRepository;
    private final AccomodationConverter accomodationConverter;
    private final int PageSize = 20;        // 숙소 목록 페이지 크기

    /**
     * 전체 숙소 목록 조회
     *
     * @param page  조회할 페이지 번호
     * @return      숙소 정보 목록을 포함한 응답 객체
     */
    public PagedResponse<AccomodationCardResponseDto> getAllAccommodations(int page, Optional<Long> memberId) {
        Page<Accomodation> accommodations = accomodationRepository.findAll(PageRequest.of(page - 1, PageSize));
        checkPageException(accommodations);
        return accomodationConverter.convertToPagedResponse(accommodations, null, null, memberId, this);
    }

    /**
     * 검색된 숙소 목록 조회
     *
     * @param keyword   숙소 이름
     * @param region    지역명
     * @param request   체크인/체크아웃 시간
     * @param page      조회할 페이지 번호
     * @return          입력된 옵션에 대한 숙소 검색결과 반환
     */

    public PagedResponse<AccomodationCardResponseDto> getAllAccommodationsBySearch(
            String keyword, String region, AccommodationRequestDto request, int page, Optional<Long> memberId
    ) {
        List<Long> keywordIList = AccommodationUtils.getIdByKeyword(keyword, accomodationSearchRepository);
        List<Long> regionIdList = AccommodationUtils.getIdByRegion(region, accomodationRepository);
        List<Long> commonIds = AccommodationUtils.getCommonId(keywordIList, regionIdList);

        Page<Accomodation> accommodations = accomodationRepository.findByIdIn(commonIds, PageRequest.of(page-1, PageSize));
        checkPageException(accommodations);
        return accomodationConverter.convertToPagedResponse(accommodations, request.getCheckIn(), request.getCheckOut(), memberId,this);
    }

    /**
     * 숙소 상세정보 조회
     *
     * @param accomodationId    숙소 id
     * @return                  숙소 정보 및 객실 목록을 포함한 응답 객체
     */
    public AccomodationDetailsResponseDto getAccomodationDetails(Long accomodationId, String checkIn, String checkOut, Optional<Long> memberId) {
        Accomodation accomodation = accomodationRepository.findById(accomodationId)
            .orElseThrow(() -> new GlobalException(AccomodationErrorCode.RESOURCE_NOT_FOUND));

        List<RoomResponseDto> rooms = getRoomResponseDto(accomodationId, checkIn, checkOut);
        List<ReviewResponseDto> reviews = getReviewResponse(accomodation.getReviews());
        Double avgStar = AccommodationUtils.calculateAverageStar(accomodation.getReviews());
        boolean isLiked = false;
        if (memberId.isPresent())
            isLiked = getIsLiked(memberId.get(), accomodationId);

        return AccomodationDetailsResponseDto.toDto(accomodation, rooms, reviews, avgStar, isLiked);
    }
    /**
     * 객실 상세정보 조회
     *
     * @param accomodationId    숙소 id
     * @param roomId            객실 id
     * @return                  객실 정보 객체
     */
    public RoomResponseDto getRoomDetail(Long accomodationId, Long roomId, String checkIn, String checkOut) {
        Room room = roomRepository.findById(roomId)
            .orElseThrow(() -> new GlobalException(AccomodationErrorCode.RESOURCE_NOT_FOUND));
        if(!accomodationId.equals(room.getAccomodation().getId()))
            throw new GlobalException(AccomodationErrorCode.INVALID_ROOM_REQUEST);
        boolean reservationAvailable = getReservationAvailable(checkIn, checkOut, roomId);
        return RoomResponseDto.toDto(room, reservationAvailable);
    }

    /**
     * 해당 객실의 예약가능 여부를 반환하는 메서드
     * @param checkIn   예약할 checkIn 정보
     * @param checkOut  예약할 checkOut 정보
     * @param roomId    조회할 객실 id
     * @return          예약가능 여부
     */
    public boolean getReservationAvailable(String checkIn, String checkOut, Long roomId) {
        List<Long> list = Collections.singletonList(roomId);
        List<LocalDateTime> checkInOut = DateTimeUtil.parseDateTimes(checkIn, checkOut);
        List<Reservation> reservations = reservationRepository.findOverlappingReservations(list, checkInOut.get(0), checkInOut.get(1));
        return reservations.isEmpty();
    }


    /**
     * 숙소 상세정보의 객실 데이터 반환 메서드
     * @param accomodationId    해당 숙소의 id
     * @param checkIn           체크인 시간 ( default: 당일 ~ 익일 )
     * @param checkOut          체크아웃 시간
     * @return                  객실 정보가 담긴 객체 리스트 반환
     */
    private List<RoomResponseDto> getRoomResponseDto(Long accomodationId, String checkIn, String checkOut) {
        return roomRepository.findByAccomodationId(accomodationId).stream()
            .map(room -> RoomResponseDto.toDto(room, getReservationAvailable(checkIn, checkOut, room.getId())))
            .toList();
    }


    /**
     * 해당 숙소의 최근 작성된 리뷰 5개를 반환하는 메서드
     * @param reviews  조회할 리뷰 리스트
     * @return         최근 작성된 리뷰 객체 리스트 반환
     */
    private List<ReviewResponseDto> getReviewResponse(List<Review> reviews) {
        return reviews.stream()
            .sorted(Comparator.comparing(Review::getCreatedAt).reversed())
            .limit(5)
            .map(review -> new ReviewResponseDto(review.getComment(), review.getStar()))
            .toList();
    }


    public boolean getIsLiked(Long memberId, Long accomodationId) {
        boolean isLiked = false;
        Optional<Like> optionalIsLiked = likeRepository.findByMemberIdAndAccomodationId(memberId, accomodationId);
        isLiked = optionalIsLiked.map(Like::isLiked).orElse(false);
        return isLiked;
    }
}