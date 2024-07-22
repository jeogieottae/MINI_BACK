package com.example.mini.domain.like.repository;

import com.example.mini.domain.like.entity.Like;
import com.example.mini.domain.member.entity.Member;
import com.example.mini.domain.accomodation.entity.Accomodation;
import com.example.mini.domain.member.repository.MemberRepository;
import com.example.mini.domain.accomodation.repository.AccomodationRepository;
import com.example.mini.domain.member.fixture.MemberEntityFixture;
import com.example.mini.domain.accomodation.fixture.AccomodationEntityFixture;
import com.example.mini.domain.like.fixture.LikeEntityFixture;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class LikeRepositoryTest { /*모두 성공*/

	@Autowired
	private LikeRepository likeRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private AccomodationRepository accomodationRepository;

	private Member testMember;
	private Accomodation testAccomodation;
	private Like testLike;

	@BeforeEach
	void setup() {
		// 기존 데이터를 삭제하여 중복을 방지
		likeRepository.deleteAll();
		memberRepository.deleteAll();
		accomodationRepository.deleteAll();

		// 엔티티를 저장하는 순서 보장
		testMember = MemberEntityFixture.getMember();
		testMember = memberRepository.saveAndFlush(testMember);

		testAccomodation = AccomodationEntityFixture.getAccomodation();
		testAccomodation = accomodationRepository.saveAndFlush(testAccomodation);

		// 엔티티가 저장되었는지 확인
		assertThat(memberRepository.findById(testMember.getId())).isPresent();
		assertThat(accomodationRepository.findById(testAccomodation.getId())).isPresent();

		// 저장된 엔티티의 ID 출력
		System.out.println("Member ID: " + testMember.getId());
		System.out.println("Accomodation ID: " + testAccomodation.getId());

		// Like 엔티티 저장
		testLike = LikeEntityFixture.getLike(testMember, testAccomodation);
		testLike = likeRepository.saveAndFlush(testLike);

		// Like 엔티티가 저장되었는지 확인
		assertThat(likeRepository.findById(testLike.getId())).isPresent();
	}
	@Test
	void testFindByMemberIdAndAccomodationId() {
		Optional<Like> foundLike = likeRepository.findByMemberIdAndAccomodationId(testMember.getId(), testAccomodation.getId());
		assertThat(foundLike).isPresent();
		assertThat(foundLike.get().getMember().getId()).isEqualTo(testMember.getId());
		assertThat(foundLike.get().getAccomodation().getId()).isEqualTo(testAccomodation.getId());
	}

	@Test
	void testFindByMemberIdAndIsLiked() {
		Pageable pageable = PageRequest.of(0, 10);
		Page<Like> likePage = likeRepository.findByMemberIdAndIsLiked(testMember.getId(), true, pageable);
		assertThat(likePage.getContent()).hasSize(1);
		assertThat(likePage.getContent().get(0).getMember().getId()).isEqualTo(testMember.getId());
		assertThat(likePage.getContent().get(0).getAccomodation().getId()).isEqualTo(testAccomodation.getId());
	}
}
