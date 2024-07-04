package com.example.mini.domain.cart.model.response;

import java.time.LocalDateTime;

import com.example.mini.domain.reservation.entity.Reservation;
import java.util.List;
import java.util.stream.Collectors;
import com.example.mini.domain.accomodation.entity.RoomImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {

  private Long roomId;
  private String accommodationName;
  private String roomName;
  private Integer baseGuests;
  private Integer maxGuests;
  private LocalDateTime checkIn;
  private LocalDateTime checkOut;
  private Integer peopleNumber;
  private Integer totalPrice;
  private Integer extraPersonCharge;
  private List<String> roomImageUrls;

  public static CartResponse toDto(Reservation reservation) {
    List<String> roomImageUrls = reservation.getRoom().getImages().stream()
        .map(RoomImage::getImgUrl)
        .collect(Collectors.toList());

    return CartResponse.builder()
        .roomId(reservation.getRoom().getId())
        .accommodationName(reservation.getRoom().getAccomodation().getName())
        .roomName(reservation.getRoom().getName())
        .baseGuests(reservation.getRoom().getBaseGuests())
        .maxGuests(reservation.getRoom().getMaxGuests())
        .checkIn(reservation.getCheckIn())
        .checkOut(reservation.getCheckOut())
        .peopleNumber(reservation.getPeopleNumber())
        .totalPrice(reservation.getTotalPrice())
        .extraPersonCharge(reservation.getRoom().getExtraPersonCharge())
        .roomImageUrls(roomImageUrls)
        .build();
  }
}