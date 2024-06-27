package com.example.mini.domain.review.repository;

import com.example.mini.domain.reservation.entity.Reservation;
import com.example.mini.domain.review.entity.Review;
import com.example.mini.domain.accomodation.entity.Accomodation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

  @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Review r WHERE r.reservation = :reservation")
  boolean existsByReservation(Reservation reservation);

  @Query("SELECT r FROM Review r WHERE r.accomodation = :accomodation ORDER BY r.createdAt DESC")
  List<Review> findTop5ByAccomodationOrderByCreatedAtDesc(Accomodation accomodation, Pageable pageable);

  @Query("SELECT ROUND(AVG(r.star), 2) FROM Review r WHERE r.accomodation = :accomodation")
  Double findAverageStarByAccomodation(Accomodation accomodation);
}