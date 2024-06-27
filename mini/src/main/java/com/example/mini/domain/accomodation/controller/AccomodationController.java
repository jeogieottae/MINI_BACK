package com.example.mini.domain.accomodation.controller;

import com.example.mini.domain.accomodation.model.request.AccomodationRequestDto;
import com.example.mini.domain.accomodation.model.response.*;
import com.example.mini.domain.accomodation.service.AccomodationService;
import com.example.mini.global.api.ApiResponse;
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
    public ResponseEntity<ApiResponse<PagedResponse<AccomodationCardResponseDto>>> getAllAccommodations(
            @RequestParam(value="page", required = false, defaultValue = "1") int page
    ) {
        PagedResponse<AccomodationCardResponseDto> response = accomodationService.getAllAccommodations(page);
        return ResponseEntity.ok(ApiResponse.OK(response));
    }

    @GetMapping("/category")
    public ResponseEntity<ApiResponse<PagedResponse<AccomodationResponseDto>>> getCategory(
            @RequestParam(value = "region", required = true) String region,
            @RequestParam(value= "page", required = false, defaultValue = "1") int page
    ) {
        PagedResponse<AccomodationResponseDto> response = accomodationService.getAccommodationsByCategory(region, page);

        return ResponseEntity.ok(ApiResponse.OK(response));
    }

    // 데이터 삽입 테스트
    @PostMapping("")
    public ResponseEntity<ApiResponse<AccomodationResponseDto>> saveTest(
            @RequestBody AccomodationRequestDto requestDto
    ) {
        AccomodationResponseDto response = accomodationService.saveAccomodation(requestDto);
        return ResponseEntity.ok(ApiResponse.OK(response));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PagedResponse<AccomodationResponseDto>>> searchByAccommodationName(
            @RequestParam(value = "name", required = true) String keyword,
            @RequestParam(value= "page", required = false, defaultValue = "1") int page
    ) {
        PagedResponse<AccomodationResponseDto> response = accomodationService.searchByAccommodationName(keyword, page);
        return ResponseEntity.ok(ApiResponse.OK(response));
    }

    @GetMapping("/{accomodationId}")
    public ResponseEntity<ApiResponse<AccomodationDetailsResponseDto>> getAccomodationDetails(
            @PathVariable Long accomodationId
    ) {
        AccomodationDetailsResponseDto response = accomodationService.getAccomodationDetails(accomodationId);
        return ResponseEntity.ok(ApiResponse.OK(response));
    }

    @GetMapping("/{accomodationId}/room/{roomId}")
    public ResponseEntity<ApiResponse<RoomResponseDto>> getRoomDetail(
            @PathVariable Long accomodationId,
            @PathVariable Long roomId
    ) {
        RoomResponseDto response = accomodationService.getRoomDetail(accomodationId, roomId);
        return ResponseEntity.ok(ApiResponse.OK(response));
    }

}
