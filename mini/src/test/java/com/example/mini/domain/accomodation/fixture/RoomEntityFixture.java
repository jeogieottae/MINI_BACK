package com.example.mini.domain.accomodation.fixture;

import com.example.mini.domain.accomodation.entity.Accomodation;
import com.example.mini.domain.accomodation.entity.Room;

public class RoomEntityFixture {

	public static Room getDefaultRoom(Accomodation accomodation) {
		return Room.builder()
			.name("테스트 객실")
			.baseGuests(2)
			.price(100000)
			.maxGuests(4)
			.extraPersonCharge(20000)
			.accomodation(accomodation)
			.build();
	}
}
