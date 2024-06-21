package com.example.mini.domain.reservation.repository;

import com.example.mini.domain.reservation.entity.Reservation;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

//  @Query("SELECT r FROM Reservation r " +
//      "JOIN r.roomList rl " +
//      "WHERE rl IN :roomIds " +
//      "AND ((r.checkIn < :checkOut AND r.checkOut > :checkIn) OR " +
//      "(r.checkIn >= :checkIn AND r.checkOut <= :checkOut) OR " +
//      "(r.checkIn < :checkOut AND r.checkOut >= :checkOut) OR " +
//      "(r.checkIn <= :checkIn AND r.checkOut > :checkIn))")
//  List<Reservation> findOverlappingReservations(
//      @Param("roomIds") List<Long> roomIds,
//      @Param("checkIn") LocalDateTime checkIn,
//      @Param("checkOut") LocalDateTime checkOut
//  );

}
