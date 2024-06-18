package com.example.mini.domain.accomodation.controller;

import com.example.mini.domain.accomodation.model.AccomodationResponseDto;
import com.example.mini.domain.accomodation.service.AccomodationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accommodation")
public class AccomodationController {

    private final AccomodationService accomodationService;

    @GetMapping("")
    public ResponseEntity<List<AccomodationResponseDto>> getAllAccomodations() {
        List<AccomodationResponseDto> response = accomodationService.getAllAccomodations();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/category")
    public String getCategory(
            @RequestParam String category
    ) {
        return category;
    }
}
