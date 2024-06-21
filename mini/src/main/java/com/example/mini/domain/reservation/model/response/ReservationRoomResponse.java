package com.example.mini.domain.reservation.model.response;

import com.example.mini.domain.accomodation.entity.Room;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class ReservationRoomResponse {

  private Long id;
  private Integer peopleNumber;
  private List<Room> rooms;
  private Integer extraCharge;
}
