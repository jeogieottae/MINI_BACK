package com.example.mini.domain.accomodation.controller;

import com.example.mini.domain.accomodation.model.AccomodationResponseDto;
import com.example.mini.domain.accomodation.model.PagedResponse;
import com.example.mini.domain.accomodation.service.AccomodationService;
import com.example.mini.global.util.APIUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            @RequestParam(value = "location", required = true) String location,
            @RequestParam(value= "page", required = false, defaultValue = "1") int page
    ) {
        PagedResponse<AccomodationResponseDto> response = accomodationService.getAccommodationsByCategory(location, page);

        return APIUtil.OK(response);
    }
}
