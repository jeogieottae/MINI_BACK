package com.example.mini.domain.accomodation.controller;

import com.example.mini.domain.accomodation.model.AccomodationResponseDto;
import com.example.mini.domain.accomodation.service.AccomodationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accommodation")
public class AccomodationController {

    private final AccomodationService accomodationService;

    @GetMapping("")
    public ResponseEntity<Page<AccomodationResponseDto>> getAllAccomodations(
            @RequestParam(value="page", required = false, defaultValue = "1") int page
    ) {
        Page<AccomodationResponseDto> response = accomodationService.getAllAccomodations(page);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<AccomodationResponseDto>> getCategory(
            @PathVariable int categoryId
    ) {
        List<AccomodationResponseDto> response = accomodationService.getAccommodationsByCategory(categoryId);

        return ResponseEntity.ok(response);
    }
}
