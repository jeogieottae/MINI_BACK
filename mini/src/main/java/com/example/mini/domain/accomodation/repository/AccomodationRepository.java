package com.example.mini.domain.accomodation.repository;

import com.example.mini.domain.accomodation.entity.Accomodation;
import com.example.mini.domain.accomodation.entity.enums.AccomodationCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AccomodationRepository extends JpaRepository<Accomodation, Long> {

    @Query("SELECT a.id FROM Accomodation a WHERE a.category = :category")
    List<Long> findByCategoryName(AccomodationCategory category);

    Page<Accomodation> findAll(Pageable pageable);

    @Query("SELECT a FROM Accomodation a WHERE a.id IN :idList")
    Page<Accomodation> findByIdList(List<Long> idList, Pageable pageable);
}