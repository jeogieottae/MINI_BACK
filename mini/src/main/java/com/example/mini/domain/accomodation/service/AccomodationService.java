package com.example.mini.domain.accomodation.service;

import com.example.mini.domain.accomodation.entity.Accomodation;
import com.example.mini.domain.accomodation.model.AccomodationRequestDto;
import com.example.mini.domain.accomodation.model.AccomodationResponseDto;
import com.example.mini.domain.accomodation.model.PagedResponse;
import com.example.mini.domain.accomodation.repository.AccomodationRepository;
import com.example.mini.domain.accomodation.repository.CategoryRepository;
import com.example.mini.global.exception.error.AccomodationErrorCode;
import com.example.mini.global.exception.type.AccomodationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AccomodationService {

    private final AccomodationRepository accomodationRepository;
    private final CategoryRepository categoryRepository;
    private final int PageSize = 5; // 페이지 크기

    public PagedResponse<AccomodationResponseDto> getAllAccommodations(int page) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.asc("name"));
        Page<Accomodation> accommodations = accomodationRepository.findAll(PageRequest.of(page-1, PageSize, Sort.by(sorts)));
        return setResponse(page, accommodations);
    }

    public PagedResponse<AccomodationResponseDto> getAccommodationsByCategory(String categoryName, int page) {
        Long categoryId = categoryRepository.findByName(categoryName);
        Page<Accomodation> accommodations = accomodationRepository.findByCategoryId(categoryId, PageRequest.of(page-1, PageSize));
        return setResponse(page, accommodations);
    }

    PagedResponse<AccomodationResponseDto> setResponse(int page, Page<Accomodation> accommodations) {
        List<AccomodationResponseDto> content = accommodations.getContent().stream()
                .map(AccomodationResponseDto::toDto)
                .toList();
        return new PagedResponse<>(accommodations.getTotalPages(), accommodations.getTotalElements(), content);
    }
}
