package com.example.mini.domain.accomodation.fixture;

import com.example.mini.domain.accomodation.entity.Accomodation;
import com.example.mini.domain.accomodation.entity.Room;
import com.example.mini.domain.accomodation.entity.enums.AccomodationCategory;

import java.time.LocalDateTime;


public class AccomodationEntityFixture {

	public static Accomodation getAccomodation() {
		return Accomodation.builder()
			.id(1L)
			.name("테스트 호텔")
			.description("묵기 좋은 호텔")
			.postalCode("12345")
			.address("테스트 주소")
			.parkingAvailable(true)
			.cookingAvailable(true)
			.checkIn(LocalDateTime.of(2023, 7, 1, 14, 0))
			.checkOut(LocalDateTime.of(2023, 7, 2, 11, 0))
			.category(AccomodationCategory.SEOUL)
			.build();
	}

	public static Room getRoom(Accomodation accomodation) {
		return Room.builder()
			.id(1L)
			.name("테스트 객실")
			.baseGuests(2)
			.price(100000)
			.maxGuests(4)
			.extraPersonCharge(20000)
			.accomodation(accomodation)
			.build();
	}

	public static Accomodation getAccomodationByCategory(AccomodationCategory category) {
		return Accomodation.builder()
			.name("테스트 호텔")
			.description("묵기 좋은 호텔")
			.postalCode("12345")
			.address("테스트 주소")
			.parkingAvailable(true)
			.cookingAvailable(true)
			.checkIn(LocalDateTime.of(2023, 7, 1, 14, 0))
			.checkOut(LocalDateTime.of(2023, 7, 2, 11, 0))
			.category(category)
			.build();
	}
}
