package com.example.mini.domain.accomodation.service;

import com.example.mini.domain.accomodation.model.response.*;
import com.example.mini.domain.reservation.entity.Reservation;
import com.example.mini.domain.reservation.repository.ReservationRepository;
import com.example.mini.global.model.dto.PagedResponse;
import com.example.mini.global.util.datetime.DateTimeUtil;
import com.example.mini.domain.accomodation.entity.Accomodation;
import com.example.mini.domain.accomodation.entity.AccomodationImage;
import com.example.mini.domain.accomodation.entity.Room;
import com.example.mini.domain.accomodation.entity.enums.AccomodationCategory;
import com.example.mini.domain.accomodation.model.response.AccomodationSearch;
import com.example.mini.domain.accomodation.repository.AccomodationRepository;
import com.example.mini.domain.accomodation.repository.AccomodationSearchRepository;
import com.example.mini.domain.accomodation.repository.RoomRepository;
import com.example.mini.domain.review.entity.Review;
import com.example.mini.domain.review.model.response.ReviewResponse;
import com.example.mini.global.api.exception.GlobalException;
import com.example.mini.global.api.exception.error.AccomodationErrorCode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Set;
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
     * 검색된 숙소 목록 조회
     *
     * @param keyword   숙소 이름
     * @param region    지역명
     * @param checkIn   체크인 시간
     * @param checkOut  체크아웃 시간
     * @param page      조회할 페이지 번호
     * @return          입력된 옵션에 대한 숙소 검색결과 반환
     */
    public PagedResponse<AccomodationCardResponseDto> searchByAccommodationName(String keyword, String region, String checkIn, String checkOut, int page) {
        List<Long> keywordIList = getIdByKeyword(keyword);
        List<Long> regionIdList = getIdByRegion(region);
        List<Long> commonIds = getCommonId(keywordIList, regionIdList);

        Page<Accomodation> accommodations = accomodationRepository.findByIdList(commonIds, PageRequest.of(page-1, PageSize));
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

        List<String> imageUrls = accomodation.getImages().stream()
            .map(AccomodationImage::getImgUrl)
            .collect(Collectors.toList());

        return AccomodationDetailsResponseDto.builder()
            .accomodation(AccomodationResponseDto.toDto(accomodation))
            .rooms(getRoomResponseDto(accomodationId, checkIn, checkOut))
            .reviews(getReviewResponse(accomodation.getReviews()))
            .avgStar(calculateAverageStar(accomodation.getReviews()))
            .imageUrls(imageUrls)
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

    private PagedResponse<AccomodationCardResponseDto> setResponse(Page<Accomodation> accommodations, String checkIn, String checkOut) {
        List<AccomodationCardResponseDto> content = accommodations.getContent().stream().map(accommodation -> {
            Integer minPrice = roomRepository.findMinPriceByAccommodationId(accommodation.getId());
            boolean isAvailable = roomRepository.findByAccomodationId(accommodation.getId())
                .stream()
                .map(room -> reservationAvailable(checkIn, checkOut, room.getId()))
                .anyMatch(Boolean::booleanValue);
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
        return roomRepository.findByAccomodationId(accomodationId).stream()
            .map(room -> RoomResponseDto.toDto(room, reservationAvailable(checkIn, checkOut, room.getId())))
            .toList();
    }

    /**
     * 리뷰 리스트를 이용하여 평균 별점을 계산하는 메서드
     *
     * @param reviews 리뷰 리스트
     * @return 평균 별점
     */

    private Double calculateAverageStar(List<Review> reviews) {
        return reviews.isEmpty() ? 0.0 :
            BigDecimal.valueOf(reviews.stream().mapToDouble(Review::getStar).average().orElse(0.0))
                .setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * 해당 숙소의 최근 작성된 리뷰 5개를 반환하는 메서드
     * @param reviews  조회할 리뷰 리스트
     * @return         최근 작성된 리뷰 객체 리스트 반환
     */
    private List<ReviewResponse> getReviewResponse(List<Review> reviews) {
        return reviews.stream()
            .sorted(Comparator.comparing(Review::getCreatedAt).reversed())
            .limit(5)
            .map(review -> new ReviewResponse(review.getComment(), review.getStar()))
            .toList();
    }

    /**
     * 입력된 숙소명에 대한 숙소 id를 반환하는 메서드
     * @param keyword   검색할 숙소명
     * @return          검색결과 id 리스트
     */
    private List<Long> getIdByKeyword(String keyword) {
        List<Long> idList;
        if (!keyword.isEmpty()) {
            List<AccomodationSearch> searches = accomodationSearchRepository.findAccommodationsByName(keyword);
            idList = searches.stream().map(AccomodationSearch::getId).toList();
        } else {
            idList = new ArrayList<>();
        }
        return idList;
    }

    /**
     * 입력된 지역명에 대한 숙소 id를 반환하는 메서드
     * @param region    검색할 지역명
     * @return          검색결과 id 리스트
     */
    private List<Long> getIdByRegion(String region) {
        List<Long> idList;
        if (!region.isEmpty()) {
            AccomodationCategory category = AccomodationCategory.fromName(region);
            idList = accomodationRepository.findByCategoryName(category);
        } else {
            idList = new ArrayList<>();
        }
        return idList;
    }

    /**
     * 두 리스트의 공통 id를 추출해 반환하는 메서드
     * 만약 한 리스트가 비어있을 경우 다른 리스트 전체를 반환
     * @param keywordIList  숙소명 검색결과 리스트
     * @param regionIdList  지역명 검색결과 리스트
     * @return              두 리스트의 공통 id 리스트
     */
    private List<Long> getCommonId(List<Long> keywordIList, List<Long> regionIdList) {
        List<Long> commonIds;
        if (regionIdList.isEmpty() && keywordIList.isEmpty()) {
            throw new GlobalException(AccomodationErrorCode.RESOURCE_NOT_FOUND);
        } else if (regionIdList.isEmpty()) {
            commonIds = keywordIList;
        } else if (keywordIList.isEmpty()) {
            commonIds = regionIdList;
        } else {
            Set<Long> idSet1 = keywordIList.stream().collect(Collectors.toSet());
            commonIds = regionIdList.stream()
                .filter(idSet1::contains)
                .collect(Collectors.toList());
        }
        return commonIds;
    }

}