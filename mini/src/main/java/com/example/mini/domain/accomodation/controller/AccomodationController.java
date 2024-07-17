package com.example.mini.domain.accomodation.controller;

import com.example.mini.domain.accomodation.model.request.AccommodationRequestDto;
import com.example.mini.domain.accomodation.model.response.*;
import com.example.mini.domain.accomodation.service.AccomodationService;
import com.example.mini.global.api.ApiResponse;
import com.example.mini.global.api.exception.success.SuccessCode;
import com.example.mini.global.model.dto.PagedResponse;
import com.example.mini.global.security.details.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accommodations")
public class AccomodationController {

    private final AccomodationService accomodationService;

    @GetMapping("")
    public ResponseEntity<ApiResponse<PagedResponse<AccomodationCardResponseDto>>> getAllAccommodations(
        @RequestParam(value="page", defaultValue = "1") int page,
        @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long memberId = (userDetails==null) ? -1L : userDetails.getMemberId();
        PagedResponse<AccomodationCardResponseDto> response = accomodationService.getAllAccommodations(page, memberId);
        return ResponseEntity.ok(ApiResponse.SUCCESS(SuccessCode.ACCOMMODATIONS_RETRIEVED, response));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PagedResponse<AccomodationCardResponseDto>>> searchByAccommodationName(
        @RequestParam(value = "query", defaultValue = "") String query,
        @RequestParam(value = "region", defaultValue = "") String region,
        @RequestParam(value = "check-in", defaultValue = "")String checkIn,
        @RequestParam(value = "check-out", defaultValue = "")String checkOut,
        @RequestParam(value= "page", defaultValue = "1") int page,
        @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long memberId = (userDetails==null) ? -1L : userDetails.getMemberId();
        PagedResponse<AccomodationCardResponseDto> response =
                accomodationService.searchByAccommodationName(query, region, checkIn, checkOut, page, memberId);
        return ResponseEntity.ok(ApiResponse.SUCCESS(SuccessCode.ACCOMMODATION_SEARCH_SUCCESS, response));    }

    @GetMapping("/{accomodationId}")
    public ResponseEntity<ApiResponse<AccomodationDetailsResponseDto>> getAccomodationDetails(
        @PathVariable Long accomodationId,
        @ModelAttribute AccommodationRequestDto request,
        @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long memberId = (userDetails==null) ? -1L : userDetails.getMemberId();
        AccomodationDetailsResponseDto response = accomodationService
            .getAccomodationDetails(accomodationId, request.getCheckIn(), request.getCheckOut(), memberId);
        return ResponseEntity.ok(ApiResponse.SUCCESS(SuccessCode.ACCOMMODATION_DETAILS_RETRIEVED, response));
    }

    @GetMapping("/{accomodationId}/room/{roomId}")
    public ResponseEntity<ApiResponse<RoomResponseDto>> getRoomDetail(
        @PathVariable Long accomodationId,
        @PathVariable Long roomId,
        @ModelAttribute AccommodationRequestDto request
    ) {
        RoomResponseDto response = accomodationService
            .getRoomDetail(accomodationId, roomId, request.getCheckIn(), request.getCheckOut());
        return ResponseEntity.ok(ApiResponse.SUCCESS(SuccessCode.ROOM_DETAILS_RETRIEVED, response));
    }

}