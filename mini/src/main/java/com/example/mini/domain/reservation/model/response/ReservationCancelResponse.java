package com.example.mini.domain.reservation.model.response;

import com.example.mini.domain.accomodation.entity.RoomImage;
import com.example.mini.domain.reservation.entity.Reservation;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationCancelResponse {
  private String memberName;
  private String accomodationName;
  private String accomodationAddress;
  private String roomName;
  private Integer roomPrice;
  private Integer extraCharge;
  private Integer totalPrice;
  private Integer peopleNumber;
  private LocalDateTime checkIn;
  private LocalDateTime checkOut;
  private List<String> roomImageUrls;

  public static ReservationCancelResponse toDto(Reservation reservation) {
    List<String> roomImageUrls = reservation.getRoom().getImages().stream()
        .map(RoomImage::getImgUrl)
        .collect(Collectors.toList());

    return ReservationCancelResponse.builder()
        .memberName(reservation.getMember().getName())
        .accomodationName(reservation.getRoom().getAccomodation().getName())
        .accomodationAddress(reservation.getRoom().getAccomodation().getAddress())
        .roomName(reservation.getRoom().getName())
        .roomPrice(reservation.getRoom().getPrice())
        .extraCharge(reservation.getExtraCharge())
        .totalPrice(reservation.getTotalPrice())
        .peopleNumber(reservation.getPeopleNumber())
        .checkIn(reservation.getCheckIn())
        .checkOut(reservation.getCheckOut())
        .roomImageUrls(roomImageUrls)
        .build();
  }
}