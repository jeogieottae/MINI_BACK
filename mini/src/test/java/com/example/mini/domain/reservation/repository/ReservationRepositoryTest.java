package com.example.mini.domain.reservation.repository;

import com.example.mini.domain.accomodation.entity.Accomodation;
import com.example.mini.domain.accomodation.entity.Room;
import com.example.mini.domain.accomodation.entity.enums.AccomodationCategory;
import com.example.mini.domain.accomodation.fixture.AccomodationEntityFixture;
import com.example.mini.domain.accomodation.fixture.RoomEntityFixture;
import com.example.mini.domain.accomodation.repository.AccomodationRepository;
import com.example.mini.domain.accomodation.repository.RoomRepository;
import com.example.mini.domain.member.entity.Member;
import com.example.mini.domain.member.fixture.MemberEntityFixture;
import com.example.mini.domain.member.repository.MemberRepository;
import com.example.mini.domain.reservation.entity.Reservation;
import com.example.mini.domain.reservation.entity.enums.ReservationStatus;
import com.example.mini.domain.reservation.fixture.ReservationEntityFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ReservationRepositoryTest {

	@Autowired
	private ReservationRepository reservationRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private AccomodationRepository accomodationRepository;

	@Autowired
	private RoomRepository roomRepository;

	private Member testMember;
	private Accomodation testAccomodation;
	private Room testRoom;
	private Reservation testReservation;

	@BeforeEach
	void setUp() {
		// 기존 데이터를 삭제하여 중복을 방지
		reservationRepository.deleteAll();
		roomRepository.deleteAll();
		accomodationRepository.deleteAll();
		memberRepository.deleteAll();

		// 테스트 엔티티 생성 및 저장
		testMember = MemberEntityFixture.getDefaultMember();
		memberRepository.save(testMember);

		testAccomodation = AccomodationEntityFixture.getAccomodationByCategory(AccomodationCategory.SEOUL);
		accomodationRepository.save(testAccomodation);

		testRoom = RoomEntityFixture.getDefaultRoom(testAccomodation);
		roomRepository.save(testRoom);

		testReservation = ReservationEntityFixture.getReservation(testMember, testAccomodation, testRoom);
		reservationRepository.save(testReservation);
	}

	@Test
	void testFindOverlappingReservations() {
		List<Long> roomIds = Arrays.asList(testRoom.getId());
		LocalDateTime checkIn = LocalDateTime.now().minusDays(3);
		LocalDateTime checkOut = LocalDateTime.now().minusDays(1);
		List<Reservation> reservations = reservationRepository.findOverlappingReservations(roomIds, checkIn, checkOut);
		assertThat(reservations).usingRecursiveFieldByFieldElementComparatorIgnoringFields("createdAt", "updatedAt", "id").contains(testReservation);
	}

	@Test
	void testFindReservationsByMemberId() {
		Pageable pageable = PageRequest.of(0, 10);
		Page<Reservation> reservationPage = reservationRepository.findReservationsByMemberId(testMember.getId(), ReservationStatus.CONFIRMED, pageable);
		assertThat(reservationPage.getContent()).hasSize(1);
		assertThat(reservationPage.getContent()).usingRecursiveFieldByFieldElementComparatorIgnoringFields("createdAt", "updatedAt", "id").contains(testReservation);
	}

	@Test
	void testFindByIdAndMemberId() {
		Optional<Reservation> foundReservation = reservationRepository.findByIdAndMemberId(testReservation.getId(), testMember.getId());
		assertThat(foundReservation).isPresent();
		assertThat(foundReservation.get().getId()).isEqualTo(testReservation.getId());
		assertThat(foundReservation.get().getMember().getId()).isEqualTo(testMember.getId());
	}

	@Test
	void testFindByMemberIdAndAccomodationIdAndStatus() {
		Optional<Reservation> foundReservation = reservationRepository.findByMemberIdAndAccomodationIdAndStatus(testMember.getId(), testAccomodation.getId(), ReservationStatus.CONFIRMED);
		assertThat(foundReservation).isPresent();
		assertThat(foundReservation.get().getId()).isEqualTo(testReservation.getId());
	}

	@Test
	void testUpdateReservationDetails() {
		LocalDateTime newCheckIn = LocalDateTime.now().plusDays(1);
		LocalDateTime newCheckOut = LocalDateTime.now().plusDays(2);
		int newPeopleNumber = 3;
		ReservationStatus newStatus = ReservationStatus.CANCELED;

		reservationRepository.updateReservationDetails(newPeopleNumber, newCheckIn, newCheckOut, newStatus, testReservation.getId());

		Optional<Reservation> updatedReservation = reservationRepository.findById(testReservation.getId());
		assertThat(updatedReservation).isPresent();
		assertThat(updatedReservation.get().getCheckIn()).isEqualTo(newCheckIn);
		assertThat(updatedReservation.get().getCheckOut()).isEqualTo(newCheckOut);
		assertThat(updatedReservation.get().getPeopleNumber()).isEqualTo(newPeopleNumber);
		assertThat(updatedReservation.get().getStatus()).isEqualTo(newStatus);
	}
}
