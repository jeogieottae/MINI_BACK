package com.example.mini.domain.accomodation.service;

import com.example.mini.domain.accomodation.entity.Accomodation;
import com.example.mini.domain.accomodation.model.AccomodationResponseDto;
import com.example.mini.domain.accomodation.repository.AccomodationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccomodationService {

    private final AccomodationRepository accomodationRepository;

    public List<AccomodationResponseDto> getAllAccomodations() {
        List<Accomodation> accomodations = accomodationRepository.findAll();
        return accomodations.stream()
                .map(AccomodationResponseDto::toDto)
                .collect(Collectors.toList());
    }

    public List<AccomodationResponseDto> getAccommodationsByCategory(int categoryId) {
        List<Accomodation> accomodations = accomodationRepository.findByCategoryId(categoryId);
        return accomodations.stream()
                .map(AccomodationResponseDto::toDto)
                .collect(Collectors.toList());
    }
}
