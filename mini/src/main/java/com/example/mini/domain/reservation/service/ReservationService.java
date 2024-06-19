package com.example.mini.domain.reservation.service;

import com.example.mini.domain.reservation.entity.Reservation;
import com.example.mini.domain.reservation.model.request.AddReservationRequest;
import com.example.mini.domain.reservation.model.response.AddReservationResponse;
import com.example.mini.domain.reservation.model.response.ReservationResponse;
import com.example.mini.domain.reservation.model.response.ReservationRoomResponse;
import com.example.mini.domain.reservation.model.response.ReservationAccomodationResponse;
import com.example.mini.domain.reservation.repository.ReservationRepository;
import com.example.mini.domain.accomodation.entity.Room;
import com.example.mini.domain.accomodation.entity.Accomodation;
import com.example.mini.domain.accomodation.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReservationService {

  @Autowired
  private ReservationRepository reservationRepository;

  @Autowired
  private RoomRepository roomRepository;

  public AddReservationResponse addReservation(AddReservationRequest request) {
    Reservation reservation = buildReservationEntity(request);
    Reservation savedReservation = reservationRepository.save(reservation);
    return buildAddReservationResponse(savedReservation);
  }

  public List<ReservationResponse> getAllReservationsForUser(Long userId) {
    List<Reservation> reservations = reservationRepository.findByUserId(userId);
    return reservations.stream()
        .map(this::mapToReservationResponse)
        .collect(Collectors.toList());
  }

  public ReservationRoomResponse getReservationById(Long reservationId) {
    Reservation reservation = reservationRepository.findById(reservationId)
        .orElseThrow(() -> new IllegalArgumentException("Reservation not found with id: " + reservationId));

    return mapToReservationRoomResponse(reservation);
  }

  private Reservation buildReservationEntity(AddReservationRequest request) {
    List<Room> rooms = roomRepository.findAllById(request.getRoomIds());
    if (rooms.isEmpty()) {
      throw new IllegalArgumentException("No rooms found for the provided room IDs");
    }

    Accomodation accommodation = rooms.get(0).getAccomodation();
    int totalExtraCharge = calculateTotalExtraCharge(rooms, Integer.parseInt(request.getPeopleNumber()));
    int totalPrice = calculateTotalPrice(rooms, totalExtraCharge);

    return Reservation.builder()
        .peopleNumber(request.getPeopleNumber())
        .extraCharge(totalExtraCharge)
        .totalPrice(totalPrice)
        .checkIn(request.getCheckIn())
        .checkOut(request.getCheckOut())
        .roomList(rooms)
        .accomodation(accommodation)
        .build();
  }

  private AddReservationResponse buildAddReservationResponse(Reservation reservation) {
    return AddReservationResponse.builder()
        .id(reservation.getId())
        .peopleNumber(reservation.getPeopleNumber())
        .extraCharge(reservation.getExtraCharge())
        .totalPrice(reservation.getTotalPrice())
        .checkIn(reservation.getCheckIn())
        .checkOut(reservation.getCheckOut())
        .roomIds(reservation.getRoomList().stream().map(Room::getId).collect(Collectors.toList()))
        .build();
  }

  private ReservationResponse mapToReservationResponse(Reservation reservation) {
    List<Long> roomIds = reservation.getRoomList().stream().map(Room::getId).collect(Collectors.toList());

    Accomodation accommodation = reservation.getAccomodation();
    ReservationAccomodationResponse accomodationResponse = mapToReservationAccomodationResponse(accommodation);

    return ReservationResponse.builder()
        .id(reservation.getId())
        .totalPrice(reservation.getTotalPrice())
        .checkIn(reservation.getCheckIn())
        .checkOut(reservation.getCheckOut())
        .roomIds(roomIds)
        .accomodationName(accomodationResponse.getName())
        .accomodationAddress(accomodationResponse.getAddress())
        .build();
  }

  private ReservationRoomResponse mapToReservationRoomResponse(Reservation reservation) {
    List<Long> roomIds = reservation.getRoomList().stream().map(Room::getId).collect(Collectors.toList());
    List<Room> rooms = roomRepository.findAllById(roomIds);

    return ReservationRoomResponse.builder()
        .id(reservation.getId())
        .peopleNumber(reservation.getPeopleNumber())
        .rooms(rooms)
        .extraCharge(reservation.getExtraCharge())
        .build();
  }

  private int calculateTotalExtraCharge(List<Room> rooms, int peopleNumber) {
    int totalExtraCharge = 0;
    for (Room room : rooms) {
      int extraGuests = peopleNumber - room.getBaseGuests();
      if (extraGuests > 0) {
        totalExtraCharge += extraGuests * room.getExtraPersonCharge();
      }
    }
    return totalExtraCharge;
  }

  private int calculateTotalPrice(List<Room> rooms, int totalExtraCharge) {
    int totalPrice = rooms.stream().mapToInt(Room::getPrice).sum();
    return totalPrice + totalExtraCharge;
  }

  private ReservationAccomodationResponse mapToReservationAccomodationResponse(Accomodation accommodation) {
    return ReservationAccomodationResponse.builder()
        .id(accommodation.getId())
        .name(accommodation.getName())
        .address(accommodation.getAddress())
        .build();
  }
}
