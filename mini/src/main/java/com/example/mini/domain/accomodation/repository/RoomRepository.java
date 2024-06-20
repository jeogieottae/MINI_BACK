package com.example.mini.domain.accomodation.repository;

import com.example.mini.domain.accomodation.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findByAccomodationId(Long accomodationId);
}
