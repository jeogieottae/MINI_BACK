package com.example.mini.domain.accomodation.controller;

import com.example.mini.domain.accomodation.model.response.*;
import com.example.mini.domain.accomodation.service.AccomodationService;
import com.example.mini.global.api.ApiResponse;
import com.example.mini.global.model.dto.PagedResponse;
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
            @RequestParam(value="page", defaultValue = "1") int page
    ) {
        PagedResponse<AccomodationCardResponseDto> response = accomodationService.getAllAccommodations(page);
        return ResponseEntity.ok(ApiResponse.OK(response));
    }

    @GetMapping("/category")
    public ResponseEntity<ApiResponse<PagedResponse<AccomodationCardResponseDto>>> getCategory(
            @RequestParam(value = "region", required = true) String region,
            @RequestParam(value = "check-in", defaultValue = "")String checkIn,
            @RequestParam(value = "check-out", defaultValue = "")String checkOut,
            @RequestParam(value= "page", defaultValue = "1") int page
    ) {
        PagedResponse<AccomodationCardResponseDto> response = accomodationService.getAccommodationsByCategory(region, page, checkIn, checkOut);

        return ResponseEntity.ok(ApiResponse.OK(response));
    }


    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PagedResponse<AccomodationCardResponseDto>>> searchByAccommodationName(
            @RequestParam(value = "name", required = true) String keyword,
            @RequestParam(value = "check-in", defaultValue = "")String checkIn,
            @RequestParam(value = "check-out", defaultValue = "")String checkOut,
            @RequestParam(value= "page", defaultValue = "1") int page
    ) {
        PagedResponse<AccomodationCardResponseDto> response = accomodationService.searchByAccommodationName(keyword, page, checkIn, checkOut);
        return ResponseEntity.ok(ApiResponse.OK(response));
    }


    @GetMapping("/{accomodationId}")
    public ResponseEntity<ApiResponse<AccomodationDetailsResponseDto>> getAccomodationDetails(
        @PathVariable Long accomodationId,
        @RequestParam(value = "check-in", defaultValue = "")String checkIn,
        @RequestParam(value = "check-out", defaultValue = "")String checkOut
    ) {
        AccomodationDetailsResponseDto response = accomodationService
                .getAccomodationDetails(accomodationId, checkIn, checkOut);
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
