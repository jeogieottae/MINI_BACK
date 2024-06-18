package com.example.mini.domain.reservation.model.response;

import com.example.mini.domain.accomodation.entity.Room;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class ReservationRoomResponse {

  private Long id;
  private String peopleNumber;
  private Integer price;
  private LocalDateTime checkIn;
  private LocalDateTime checkOut;
  private List<Room> rooms;
}
