package com.example.mini.domain.accomodation.repository;

import com.example.mini.domain.accomodation.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findByAccomodationId(Long accomodationId);

    @Query("SELECT MIN(r.price) FROM Room r WHERE r.accomodation.id = :accommodationId")
    Integer findMinPriceByAccommodationId(@Param("accommodationId") Long accommodationId);
}
