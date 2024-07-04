package com.example.mini.domain.accomodation.fixture;

import com.example.mini.domain.accomodation.entity.Accomodation;
import com.example.mini.domain.accomodation.entity.AccomodationImage;
import com.example.mini.domain.accomodation.entity.Room;
import com.example.mini.domain.accomodation.entity.RoomImage;
import com.example.mini.domain.accomodation.entity.enums.AccomodationCategory;
import org.testcontainers.shaded.com.google.common.collect.Lists;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

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
			.images(getAccomodationImageUrl())
			.reviews(Lists.newArrayList())
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
			.images(getRoomImageUrl())
			.build();
	}

	public static Accomodation getAccomodation1(AccomodationCategory category) {
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

	public static List<Accomodation> getAccomodationList() {
		Accomodation accomodation1 = Accomodation.builder()
				.id(1L)
				.name("제주도 펜션")
				.description("묵기 좋은 호텔")
				.postalCode("12345")
				.address("테스트 주소")
				.parkingAvailable(true)
				.cookingAvailable(true)
				.checkIn(LocalDateTime.of(2023, 7, 1, 14, 0))
				.checkOut(LocalDateTime.of(2023, 7, 2, 11, 0))
				.category(AccomodationCategory.JEJU)
				.images(getAccomodationImageUrl())
				.build();

		Accomodation accomodation2 = Accomodation.builder()
				.id(2L)
				.name("제주도 호텔")
				.description("묵기 좋은 호텔")
				.postalCode("12345")
				.address("테스트 주소")
				.parkingAvailable(true)
				.cookingAvailable(true)
				.checkIn(LocalDateTime.of(2023, 7, 1, 14, 0))
				.checkOut(LocalDateTime.of(2023, 7, 2, 11, 0))
				.category(AccomodationCategory.JEJU)
				.images(getAccomodationImageUrl())
				.build();
		return Lists.newArrayList(accomodation1, accomodation2);
	}

	public static List<Room> getRoomList() {
		Accomodation accomodation = Accomodation.builder()
				.id(2L)
				.name("제주도 호텔")
				.description("묵기 좋은 호텔")
				.postalCode("12345")
				.address("테스트 주소")
				.parkingAvailable(true)
				.cookingAvailable(true)
				.checkIn(LocalDateTime.of(2023, 7, 1, 14, 0))
				.checkOut(LocalDateTime.of(2023, 7, 2, 11, 0))
				.category(AccomodationCategory.JEJU)
				.images(getAccomodationImageUrl())
				.reviews(Lists.newArrayList())
				.build();
		Room room1 = Room.builder()
				.id(1L)
				.name("테스트 객실")
				.baseGuests(2)
				.price(100000)
				.maxGuests(4)
				.extraPersonCharge(20000)
				.accomodation(accomodation)
				.images(getRoomImageUrl())
				.build();
		Room room2 = Room.builder()
				.id(2L)
				.name("테스트 객실")
				.baseGuests(2)
				.price(100000)
				.maxGuests(4)
				.extraPersonCharge(20000)
				.accomodation(accomodation)
				.images(getRoomImageUrl())
				.build();
		return Lists.newArrayList(room1, room2);
	}

	public static List<AccomodationImage> getAccomodationImageUrl() {
		Accomodation accomodation = Accomodation.builder()
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

		return Arrays.asList(
				AccomodationImage.builder()
						.id(1L)
						.imgUrl("image url 1")
						.accomodation(accomodation)
						.build(),
				AccomodationImage.builder()
						.id(2L)
						.imgUrl("image url 2")
						.accomodation(accomodation)
						.build()
		);
	}

	public static List<RoomImage> getRoomImageUrl() {
		Accomodation accomodation = Accomodation.builder()
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
				.images(getAccomodationImageUrl())
				.build();
		Room room = Room.builder()
				.id(1L)
				.name("테스트 객실")
				.baseGuests(2)
				.price(100000)
				.maxGuests(4)
				.extraPersonCharge(20000)
				.accomodation(accomodation)
				.build();
		return Arrays.asList(
				RoomImage.builder()
						.id(1L)
						.imgUrl("image url 1")
						.room(room)
						.build(),
				RoomImage.builder()
						.id(2L)
						.imgUrl("image url 2")
						.room(room)
						.build()
		);

	}
}
