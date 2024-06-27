package com.example.mini.domain.review.repository;

import com.example.mini.domain.review.entity.Review;
import com.example.mini.domain.accomodation.entity.Accomodation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
  List<Review> findByAccomodation(Accomodation accomodation);
}