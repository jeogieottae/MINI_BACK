package com.example.mini.domain.accomodation.repository;

import com.example.mini.domain.accomodation.model.response.AccomodationSearchResponseDto;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface AccomodationSearchRepository extends ElasticsearchRepository<AccomodationSearchResponseDto, Long> {
    List<AccomodationSearchResponseDto> findAccommodationsByName(String keyword);
}
