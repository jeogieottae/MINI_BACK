package com.example.mini.domain.member.repository;

import com.example.mini.domain.member.entity.Member;
import com.example.mini.domain.member.fixture.MemberEntityFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class MemberRepositoryTest {

	@Autowired
	private MemberRepository memberRepository;

	private Member testMember;

	@BeforeEach
	void setUp() {
		// 기존 데이터를 삭제하여 중복을 방지
		memberRepository.deleteAll();

		// 테스트 엔티티 생성 및 저장
		testMember = MemberEntityFixture.getDefaultMember();
		memberRepository.save(testMember);
	}

	@Test
	void testFindByEmail() {
		Optional<Member> foundMember = memberRepository.findByEmail(testMember.getEmail());
		assertThat(foundMember).isPresent();
		assertThat(foundMember.get().getEmail()).isEqualTo(testMember.getEmail());
	}

	@Test
	void testExistsByEmail() {
		boolean exists = memberRepository.existsByEmail(testMember.getEmail());
		assertThat(exists).isTrue();
	}

	@Test
	void testExistsByNickname() {
		boolean exists = memberRepository.existsByNickname(testMember.getNickname());
		assertThat(exists).isTrue();
	}
}
