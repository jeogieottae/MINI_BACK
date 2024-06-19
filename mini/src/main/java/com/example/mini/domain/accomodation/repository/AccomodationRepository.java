package com.example.mini.domain.accomodation.repository;

import com.example.mini.domain.accomodation.entity.Accomodation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccomodationRepository extends JpaRepository<Accomodation, Long> {

    Page<Accomodation> findByCategoryId(Long categoryId, Pageable pageable);

    Page<Accomodation> findAll(Pageable pageable);
}