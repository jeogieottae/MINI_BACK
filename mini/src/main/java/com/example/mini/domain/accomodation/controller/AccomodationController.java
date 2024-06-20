package com.example.mini.domain.accomodation.controller;

import com.example.mini.domain.accomodation.model.*;
import com.example.mini.domain.accomodation.service.AccomodationService;
import com.example.mini.global.util.APIUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accommodation")
public class AccomodationController {

    private final AccomodationService accomodationService;

    @GetMapping("")
    public ResponseEntity<PagedResponse<AccomodationResponseDto>> getAllAccommodations(
            @RequestParam(value="page", required = false, defaultValue = "1") int page
    ) {
        PagedResponse<AccomodationResponseDto> response = accomodationService.getAllAccommodations(page);
        return APIUtil.OK(response);
    }

    @GetMapping("/category")
    public ResponseEntity<PagedResponse<AccomodationResponseDto>> getCategory(
            @RequestParam(value = "region", required = true) String region,
            @RequestParam(value= "page", required = false, defaultValue = "1") int page
    ) {
        PagedResponse<AccomodationResponseDto> response = accomodationService.getAccommodationsByCategory(region, page);

        return APIUtil.OK(response);
    }

    // 데이터 삽입 테스트
    @PostMapping("")
    public ResponseEntity<AccomodationResponseDto> saveTest(
            @RequestBody AccomodationRequestDto requestDto
    ) {
        AccomodationResponseDto dto = accomodationService.saveAccomodation(requestDto);
        return APIUtil.OK(dto);
    }

    @GetMapping("/search")
    public ResponseEntity<PagedResponse<AccomodationResponseDto>> searchByAccommodationName(
            @RequestParam(value = "name", required = true) String keyword,
            @RequestParam(value= "page", required = false, defaultValue = "1") int page
    ) {
        PagedResponse<AccomodationResponseDto> response = accomodationService.searchByAccommodationName(keyword, page);
        return APIUtil.OK(response);
    }

    @GetMapping("/{accomodationId}")
    public ResponseEntity<AccomodationDetailsResponseDto> getAccomodationDetails(
            @PathVariable Long accomodationId
    ) {
        AccomodationDetailsResponseDto response = accomodationService.getAccomodationDetails(accomodationId);
        return APIUtil.OK(response);
    }

    @GetMapping("/{accomodationId}/room/{roomId}")
    public ResponseEntity<RoomResponseDto> getRoomDetail(
            @PathVariable Long accomodationId,
            @PathVariable Long roomId
    ) {
        RoomResponseDto response = accomodationService.getRoomDetail(accomodationId, roomId);
        return APIUtil.OK(response);
    }

}
