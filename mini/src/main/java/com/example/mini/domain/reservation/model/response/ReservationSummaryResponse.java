package com.example.mini.domain.reservation.model.response;

import com.example.mini.domain.reservation.entity.Reservation;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import com.example.mini.domain.accomodation.entity.AccomodationImage;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationSummaryResponse {
  private Long reservationId;
  private String accomodationName;
  private String accomodationAddress;
  private String roomName;
  private Integer totalPrice;
  private Integer peopleNumber;
  private LocalDateTime checkIn;
  private LocalDateTime checkOut;
  private List<String> accomodationImageUrls; // 숙소 이미지

  public static ReservationSummaryResponse toDto(Reservation reservation) {
    List<String> accomodationImageUrls = reservation.getRoom().getAccomodation().getImages().stream()
        .map(AccomodationImage::getImgUrl)
        .collect(Collectors.toList());

    return ReservationSummaryResponse.builder()
        .reservationId(reservation.getId())
        .accomodationName(reservation.getRoom().getAccomodation().getName())
        .accomodationAddress(reservation.getRoom().getAccomodation().getAddress())
        .roomName(reservation.getRoom().getName())
        .totalPrice(reservation.getTotalPrice())
        .peopleNumber(reservation.getPeopleNumber())
        .checkIn(reservation.getCheckIn())
        .checkOut(reservation.getCheckOut())
        .accomodationImageUrls(accomodationImageUrls)
        .build();
  }
}
