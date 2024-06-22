package com.example.mini.domain.reservation.repository;

import com.example.mini.domain.accomodation.entity.Room;
import com.example.mini.domain.reservation.entity.Reservation;
import com.example.mini.domain.reservation.entity.enums.ReservationStatus;
import com.example.mini.domain.reservation.model.response.ReservationDetailResponse;
import com.example.mini.domain.reservation.model.response.ReservationSummaryResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

  @Query("SELECT r FROM Reservation r " +
      "WHERE r.room.id IN :roomIds " +
      "AND r.status = 'CONFIRMED' " +
      "AND ((r.checkIn < :checkOut AND r.checkOut > :checkIn) OR " +
      "(r.checkIn >= :checkIn AND r.checkOut <= :checkOut) OR " +
      "(r.checkIn < :checkOut AND r.checkOut >= :checkOut) OR " +
      "(r.checkIn <= :checkIn AND r.checkOut > :checkIn))")
  List<Reservation> findOverlappingReservations(
      @Param("roomIds") List<Long> roomIds,
      @Param("checkIn") LocalDateTime checkIn,
      @Param("checkOut") LocalDateTime checkOut
  );

  @Modifying
  @Query("UPDATE Reservation r SET r.status = :status WHERE r.id = :id")
  void updateReservationStatus(@Param("id") Long id, @Param("status") ReservationStatus status);

  Optional<Reservation> findByIdAndMemberId(Long reservationId, Long memberId);
  List<Reservation> findByMemberIdAndStatus(Long memberId, ReservationStatus status);
}
