package com.example.mini.domain.accomodation.service;

import com.example.mini.domain.accomodation.entity.Accomodation;
import com.example.mini.domain.accomodation.entity.Category;
import com.example.mini.domain.accomodation.entity.Room;
import com.example.mini.domain.accomodation.model.request.AccomodationRequestDto;
import com.example.mini.domain.accomodation.model.AccomodationSearch;
import com.example.mini.domain.accomodation.model.response.AccomodationDetailsResponseDto;
import com.example.mini.domain.accomodation.model.response.AccomodationResponseDto;
import com.example.mini.domain.accomodation.model.response.PagedResponse;
import com.example.mini.domain.accomodation.model.response.RoomResponseDto;
import com.example.mini.domain.accomodation.repository.AccomodationRepository;
import com.example.mini.domain.accomodation.repository.AccomodationSearchRepository;
import com.example.mini.domain.accomodation.repository.CategoryRepository;
import com.example.mini.domain.accomodation.repository.RoomRepository;
import com.example.mini.global.api.exception.GlobalException;
import com.example.mini.global.api.exception.error.AccomodationErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AccomodationService {

    private final AccomodationRepository accomodationRepository;
    private final CategoryRepository categoryRepository;
    private final AccomodationSearchRepository accomodationSearchRepository;
    private final RoomRepository roomRepository;
    private final int PageSize = 5; // 페이지 크기

    /**
     * 전체 숙소 목록 조회
     *
     * @param page  조회할 페이지 번호
     * @return      숙소 정보 목록을 포함한 응답 객체
     */
    public PagedResponse<AccomodationResponseDto> getAllAccommodations(int page) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.asc("name"));
        Page<Accomodation> accommodations = accomodationRepository.findAll(PageRequest.of(page-1, PageSize, Sort.by(sorts)));
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
        Long categoryId = categoryRepository.findByName(categoryName);
        if (categoryId == null) {
            throw new GlobalException(AccomodationErrorCode.INVALID_CATEGORY_CODE_REQUEST);
        }
        Page<Accomodation> accommodations = accomodationRepository.findByCategoryId(categoryId, PageRequest.of(page-1, PageSize));
        checkPageException(accommodations);
        return setResponse(accommodations);
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

    // elastic 데이터 삽입 테스트
    public AccomodationResponseDto saveAccomodation(AccomodationRequestDto requestDto) {
        Optional<Category> category = categoryRepository.findById(5L);
        Accomodation accomodation = Accomodation.builder()
                .name(requestDto.getName())
                .description(requestDto.getDescription())
                .postalCode(123445)
                .address("서귀포시 --- ---")
                .parkingAvailable(true)
                .cookingAvailable(true)
                .checkIn(LocalDateTime.now())
                .checkOut(LocalDateTime.now())
                .category(category.get())
                .build();
        Accomodation saved = accomodationRepository.save(accomodation);
        AccomodationSearch search = new AccomodationSearch(saved.getId(), saved.getName());
        accomodationSearchRepository.save(search);
        return AccomodationResponseDto.toDto(saved);
    }

    /**
     * 숙소 이름으로 검색
     *
     * @param keyword   검색 키워드
     * @return          숙소 정보 목록을 포함한 응답 객체
     */
    public PagedResponse<AccomodationResponseDto> searchByAccommodationName(String keyword, int page) {
        List<AccomodationSearch> searches = accomodationSearchRepository.findAccommodationsByName(keyword);
        List<Long> idList = searches.stream().map(AccomodationSearch::getId).toList();
        Page<Accomodation> accommodations = accomodationRepository.findByIdList(idList, PageRequest.of(page-1, PageSize));
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

        AccomodationResponseDto accomodationResponseDto = AccomodationResponseDto.toDto(accomodation);
        List<RoomResponseDto> roomResponseDtos = rooms.stream().map(RoomResponseDto::toDto).toList();

        return AccomodationDetailsResponseDto.builder()
                .accomodation(accomodationResponseDto)
                .rooms(roomResponseDtos)
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

    // 공통 에러처리
    private void checkPageException(Page<Accomodation> accommodations) {
        if (accommodations.getContent().isEmpty()) {
            throw new GlobalException(AccomodationErrorCode.RESOURCE_NOT_FOUND);
        }
    }
}
