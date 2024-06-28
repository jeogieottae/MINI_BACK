package com.example.mini.domain.accomodation.repository;

import com.example.mini.domain.accomodation.model.response.AccomodationSearch;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccomodationSearchRepository extends ElasticsearchRepository<AccomodationSearch, Long> {
    List<AccomodationSearch> findAccommodationsByName(String keyword);
}
