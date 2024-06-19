package com.example.mini.domain.accomodation.repository;

import com.example.mini.domain.accomodation.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    @Query("SELECT c.id FROM Category c WHERE c.name = :name")
    Long findByName(String name);
}
