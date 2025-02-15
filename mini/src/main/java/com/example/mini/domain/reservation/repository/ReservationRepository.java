package com.example.mini.domain.reservation.repository;

import com.example.mini.domain.reservation.entity.Reservation;
import com.example.mini.domain.reservation.entity.enums.ReservationStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

@Transactional
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

  @Query("SELECT r FROM Reservation r " +
      "WHERE r.member.id = :memberId " +
      "AND r.status = :status " +
      "ORDER BY r.checkIn ASC")
  Page<Reservation> findReservationsByMemberId(
      @Param("memberId") Long memberId,
      @Param("status") ReservationStatus status,
      Pageable pageable
  );

  @Modifying
  @Query("UPDATE Reservation r SET " +
      "r.peopleNumber = :peopleNumber, " +
      "r.checkIn = :checkIn, " +
      "r.checkOut = :checkOut, " +
      "r.status = :status " +
      "WHERE r.id = :id")
  void updateReservationDetails(
      @Param("peopleNumber") int peopleNumber,
      @Param("checkIn") LocalDateTime checkIn,
      @Param("checkOut") LocalDateTime checkOut,
      @Param("status") ReservationStatus status,
      @Param("id") Long id
  );

  @Modifying
  @Query("UPDATE Reservation r SET r.status = :status WHERE r.id = :id")
  void cancelReservation(@Param("id") Long id, @Param("status") ReservationStatus status);

  Optional<Reservation> findByIdAndMemberId(Long reservationId, Long memberId);

  @Query("SELECT r FROM Reservation r " +
      "WHERE r.member.id = :memberId " +
      "AND r.room.id IN :roomId " +
      "AND r.status = 'CONFIRMED' " +
      "AND ((r.checkIn < :checkOut AND r.checkOut > :checkIn) OR " +
      "(r.checkIn >= :checkIn AND r.checkOut <= :checkOut) OR " +
      "(r.checkIn < :checkOut AND r.checkOut >= :checkOut) OR " +
      "(r.checkIn <= :checkIn AND r.checkOut > :checkIn))")
  List<Reservation> findOverlappingReservationsByMemberId(
      @Param("memberId") Long memberId,
      @Param("roomId") Long roomId,
      @Param("checkIn") LocalDateTime checkIn,
      @Param("checkOut") LocalDateTime checkOut
  );

  Optional<Reservation> findByMemberIdAndAccomodationIdAndStatus(Long memberId, Long accomodationId, ReservationStatus status);
}
