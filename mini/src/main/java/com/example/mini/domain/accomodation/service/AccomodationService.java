package com.example.mini.domain.accomodation.service;

import com.example.mini.domain.accomodation.entity.Accomodation;
import com.example.mini.domain.accomodation.model.AccomodationResponseDto;
import com.example.mini.domain.accomodation.repository.AccomodationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccomodationService {

    private final AccomodationRepository accomodationRepository;

    public Page<AccomodationResponseDto> getAllAccomodations(int page) {
//        Page<Accomodation> accomodations = accomodationRepository.findAllOrderByName();
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.asc("name"));
        int size = 5;

        Page<Accomodation> accomodations = accomodationRepository.findAll(PageRequest.of(page-1, size, Sort.by(sorts)));
        Page<AccomodationResponseDto> dtos;
        return accomodations.map(AccomodationResponseDto::toDto);
    }

    public List<AccomodationResponseDto> getAccommodationsByCategory(int categoryId) {
        List<Accomodation> accomodations = accomodationRepository.findByCategoryId(categoryId);
        return accomodations.stream()
                .map(AccomodationResponseDto::toDto)
                .collect(Collectors.toList());
    }
}
