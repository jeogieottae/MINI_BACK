package com.example.mini.domain.cart.repository;

import com.example.mini.domain.cart.entity.Cart;
import com.example.mini.domain.member.entity.Member;
import com.example.mini.domain.member.fixture.MemberEntityFixture;
import com.example.mini.domain.cart.fixture.CartEntityFixture;
import com.example.mini.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class CartRepositoryTest { /*모두 통과*/

	@Autowired
	private CartRepository cartRepository;

	@Autowired
	private MemberRepository memberRepository;

	private Member testMember;
	private Cart testCart;

	@BeforeEach
	@Transactional
	void setUp() {
		// 기존 데이터를 삭제하여 중복을 방지
		cartRepository.deleteAll();
		memberRepository.deleteAll();

		// 테스트 엔티티 생성 및 저장
		testMember = MemberEntityFixture.getDefaultMember();
		memberRepository.save(testMember);

		testCart = CartEntityFixture.getCart(testMember);
		testCart = cartRepository.save(testCart);

		// 영속성 컨텍스트를 통해 연관 관계 설정
		testMember = memberRepository.findById(testMember.getId()).get();
		testCart = cartRepository.findById(testCart.getId()).get();

		testCart = cartRepository.save(testCart);
	}

	@Test
	void testFindByMember() {
		Optional<Cart> foundCart = cartRepository.findByMember(testMember);
		assertThat(foundCart).isPresent();
		assertThat(foundCart.get().getMember().getId()).isEqualTo(testMember.getId());
	}
}
