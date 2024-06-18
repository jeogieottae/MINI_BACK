package com.example.mini.domain.accomodation.repository;

import com.example.mini.domain.accomodation.entity.Accomodation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccomodationRepository extends JpaRepository<Accomodation, Long> {

    @Query(value = "SELECT * FROM accomodation WHERE category_id = :categoryId", nativeQuery = true)
    List<Accomodation> findByCategoryId(int categoryId);
}
