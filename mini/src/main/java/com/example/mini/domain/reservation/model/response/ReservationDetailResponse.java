package com.example.mini.domain.reservation.model.response;

import com.example.mini.domain.reservation.entity.Reservation;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import com.example.mini.domain.accomodation.entity.image.RoomImage;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationDetailResponse {
  private String memberName;
  private String accomodationName;
  private String roomName;
  private Integer roomPrice;
  private Integer baseGuests;
  private Integer extraCharge;
  private LocalDateTime checkIn;
  private LocalDateTime checkOut;
  private Boolean parkingAvailable;
  private Boolean cookingAvailable;
  private List<String> roomImageUrls;

  public static ReservationDetailResponse fromEntity(Reservation reservation) {
    List<String> roomImageUrls = reservation.getRoom().getImages().stream()
        .map(RoomImage::getImgUrl)
        .collect(Collectors.toList());

    return ReservationDetailResponse.builder()
        .memberName(reservation.getMember().getName())
        .accomodationName(reservation.getRoom().getAccomodation().getName())
        .roomName(reservation.getRoom().getName())
        .roomPrice(reservation.getRoom().getPrice())
        .baseGuests(reservation.getRoom().getBaseGuests())
        .extraCharge(reservation.getExtraCharge())
        .checkIn(reservation.getCheckIn())
        .checkOut(reservation.getCheckOut())
        .parkingAvailable(reservation.getRoom().getAccomodation().getParkingAvailable())
        .cookingAvailable(reservation.getRoom().getAccomodation().getCookingAvailable())
        .roomImageUrls(roomImageUrls)
        .build();
  }
}