package com.example.mini.domain.accomodation.service;

import com.example.mini.domain.accomodation.entity.Accomodation;
import com.example.mini.domain.accomodation.model.AccomodationResponseDto;
import com.example.mini.domain.accomodation.repository.AccomodationRepository;
import com.example.mini.domain.accomodation.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccomodationService {

    private final AccomodationRepository accomodationRepository;
    private final CategoryRepository categoryRepository;
    private final int PageSize = 5; // 페이지 크기

    public Page<AccomodationResponseDto> getAllAccommodations(int page) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.asc("name"));
        Page<Accomodation> accommodations = accomodationRepository.findAll(PageRequest.of(page-1, PageSize, Sort.by(sorts)));
        return accommodations.map(AccomodationResponseDto::toDto);
    }

    public Page<AccomodationResponseDto> getAccommodationsByCategory(String categoryName, int page) {
        Long categoryId = categoryRepository.findByName(categoryName);
        Page<Accomodation> accommodations = accomodationRepository.findByCategoryId(categoryId, PageRequest.of(page-1, PageSize));
        return accommodations.map(AccomodationResponseDto::toDto);
    }
}
