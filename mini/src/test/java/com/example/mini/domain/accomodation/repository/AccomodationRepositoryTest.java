package com.example.mini.domain.accomodation.repository;

import com.example.mini.domain.accomodation.entity.Accomodation;
import com.example.mini.domain.accomodation.entity.enums.AccomodationCategory;
import com.example.mini.domain.accomodation.fixture.AccomodationEntityFixture;
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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class AccomodationRepositoryTest { /*모두 통과*/

	@Autowired
	private AccomodationRepository accomodationRepository;

	private Accomodation testAccomodation1;
	private Accomodation testAccomodation2;

	@BeforeEach
	void setUp() {
		// 기존 데이터를 삭제하여 중복을 방지
		accomodationRepository.deleteAll();

		// 테스트 엔티티 생성 및 저장
		testAccomodation1 = AccomodationEntityFixture.getAccomodation1(AccomodationCategory.SEOUL);
		testAccomodation2 = AccomodationEntityFixture.getAccomodation1(AccomodationCategory.BUSAN);
		accomodationRepository.saveAll(Arrays.asList(testAccomodation1, testAccomodation2));
	}

	@Test
	void testFindByCategoryName() {
		List<Long> seoulAccomodations = accomodationRepository.findByCategoryName(AccomodationCategory.SEOUL);
		assertThat(seoulAccomodations).contains(testAccomodation1.getId());
		assertThat(seoulAccomodations).doesNotContain(testAccomodation2.getId());
	}

	@Test
	void testFindAll() {
		Pageable pageable = PageRequest.of(0, 10);
		Page<Accomodation> accomodationPage = accomodationRepository.findAll(pageable);
		assertThat(accomodationPage.getContent()).hasSize(2);
	}

	@Test
	void testFindByIdList() {
		List<Long> idList = Arrays.asList(testAccomodation1.getId(), testAccomodation2.getId());
		Pageable pageable = PageRequest.of(0, 10);
		Page<Accomodation> accomodationPage = accomodationRepository.findByIdList(idList, pageable);
		List<Long> returnedIds = accomodationPage.getContent().stream().map(Accomodation::getId).collect(Collectors.toList());
		assertThat(returnedIds).containsExactlyInAnyOrderElementsOf(idList);
	}
}
