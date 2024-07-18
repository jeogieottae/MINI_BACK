package com.example.mini.domain.accomodation.controller;

import com.example.mini.domain.accomodation.model.request.AccommodationRequestDto;
import com.example.mini.domain.accomodation.model.response.AccomodationCardResponseDto;
import com.example.mini.domain.accomodation.model.response.AccomodationDetailsResponseDto;
import com.example.mini.domain.accomodation.model.response.RoomResponseDto;
import com.example.mini.domain.accomodation.service.AccomodationService;
import com.example.mini.global.api.ApiResponse;
import com.example.mini.global.api.exception.success.SuccessCode;
import com.example.mini.global.model.dto.PagedResponse;
import com.example.mini.global.security.details.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

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
        Optional<Long> memberId = (userDetails==null) ? Optional.empty() :userDetails.getMemberId().describeConstable();
        PagedResponse<AccomodationCardResponseDto> response = accomodationService.getAllAccommodations(page, memberId);
        return ResponseEntity.ok(ApiResponse.SUCCESS(SuccessCode.ACCOMMODATIONS_RETRIEVED, response));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PagedResponse<AccomodationCardResponseDto>>> getAllAccommodationsBySearch(
        @RequestParam(value = "accommodationName", defaultValue = "") String name,
        @RequestParam(value = "region", defaultValue = "") String region,
        @ModelAttribute AccommodationRequestDto request,
        @RequestParam(value= "page", defaultValue = "1") int page,
        @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Optional<Long> memberId = (userDetails==null) ? Optional.empty() :userDetails.getMemberId().describeConstable();
        PagedResponse<AccomodationCardResponseDto> response =
                accomodationService.getAllAccommodationsBySearch(name, region, request, page, memberId);
        return ResponseEntity.ok(ApiResponse.SUCCESS(SuccessCode.ACCOMMODATION_SEARCH_SUCCESS, response));    }

    @GetMapping("/{accomodationId}")
    public ResponseEntity<ApiResponse<AccomodationDetailsResponseDto>> getAccomodationDetails(
        @PathVariable Long accomodationId,
        @ModelAttribute AccommodationRequestDto request,
        @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Optional<Long> memberId = (userDetails==null) ? Optional.empty() :userDetails.getMemberId().describeConstable();
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