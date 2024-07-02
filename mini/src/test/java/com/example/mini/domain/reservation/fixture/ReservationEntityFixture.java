package com.example.mini.domain.reservation.fixture;

import com.example.mini.domain.accomodation.entity.Accomodation;
import com.example.mini.domain.accomodation.entity.Room;
import com.example.mini.domain.member.entity.Member;
import com.example.mini.domain.reservation.entity.Reservation;
import com.example.mini.domain.reservation.entity.enums.ReservationStatus;
import java.time.LocalDateTime;

public class ReservationEntityFixture {

	public static Reservation getReservation(Member member, Accomodation accomodation, Room room) {
		return Reservation.builder()
			.id(1L)
			.peopleNumber(2)
			.extraCharge(0)
			.totalPrice(100000)
			.checkIn(LocalDateTime.now().minusDays(2))
			.checkOut(LocalDateTime.now().minusDays(1))
			.accomodation(accomodation)
			.member(member)
			.room(room)
			.status(ReservationStatus.CONFIRMED)
			.build();
	}

}