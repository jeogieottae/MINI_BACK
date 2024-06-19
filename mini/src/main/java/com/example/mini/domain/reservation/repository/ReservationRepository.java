package com.example.mini.domain.reservation.repository;

import com.example.mini.domain.reservation.entity.Reservation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
  List<Reservation> findByMemberId(Long memberId);
}
