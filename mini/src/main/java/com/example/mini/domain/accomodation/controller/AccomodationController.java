package com.example.mini.domain.accomodation.controller;

import com.example.mini.domain.accomodation.model.response.*;
import com.example.mini.domain.accomodation.service.AccomodationService;
import com.example.mini.global.api.ApiResponse;
import com.example.mini.global.api.exception.success.SuccessCode;
import com.example.mini.global.model.dto.PagedResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accommodation")
public class AccomodationController {

    private final AccomodationService accomodationService;

    @GetMapping("")
    public ResponseEntity<ApiResponse<PagedResponse<AccomodationCardResponseDto>>> getAllAccommodations(
            @RequestParam(value="page", defaultValue = "1") int page
    ) {
        PagedResponse<AccomodationCardResponseDto> response = accomodationService.getAllAccommodations(page);
        return ResponseEntity.ok(ApiResponse.SUCCESS(SuccessCode.ACCOMMODATIONS_RETRIEVED, response));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PagedResponse<AccomodationCardResponseDto>>> searchByAccommodationName(
            @RequestParam(value = "query", defaultValue = "") String query,
            @RequestParam(value = "region", defaultValue = "") String region,
            @RequestParam(value = "check-in", defaultValue = "")String checkIn,
            @RequestParam(value = "check-out", defaultValue = "")String checkOut,
            @RequestParam(value= "page", defaultValue = "1") int page
    ) {
        PagedResponse<AccomodationCardResponseDto> response = accomodationService.searchByAccommodationName(query, region, checkIn, checkOut, page);
        return ResponseEntity.ok(ApiResponse.SUCCESS(SuccessCode.ACCOMMODATION_SEARCH_SUCCESS, response));    }

    @GetMapping("/{accomodationId}")
    public ResponseEntity<ApiResponse<AccomodationDetailsResponseDto>> getAccomodationDetails(
        @PathVariable Long accomodationId,
        @RequestParam(value = "check-in", defaultValue = "")String checkIn,
        @RequestParam(value = "check-out", defaultValue = "")String checkOut
    ) {
        AccomodationDetailsResponseDto response = accomodationService
                .getAccomodationDetails(accomodationId, checkIn, checkOut);
        return ResponseEntity.ok(ApiResponse.SUCCESS(SuccessCode.ACCOMMODATION_DETAILS_RETRIEVED, response));
    }

    @GetMapping("/{accomodationId}/room/{roomId}")
    public ResponseEntity<ApiResponse<RoomResponseDto>> getRoomDetail(
        @PathVariable Long accomodationId,
        @PathVariable Long roomId
    ) {
        RoomResponseDto response = accomodationService.getRoomDetail(accomodationId, roomId);
        return ResponseEntity.ok(ApiResponse.SUCCESS(SuccessCode.ROOM_DETAILS_RETRIEVED, response));
    }

}
