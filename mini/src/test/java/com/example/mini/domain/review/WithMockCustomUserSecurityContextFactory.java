package com.example.mini.domain.review;

import com.example.mini.domain.member.entity.Member;
import com.example.mini.domain.member.entity.enums.MemberState;
import com.example.mini.global.security.details.UserDetailsImpl;
import java.time.LocalDateTime;
import java.util.Collections;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithMockUserDetailsSecurityContextFactory implements
	WithSecurityContextFactory<WithMockCustomUser> {

	@Override
	public SecurityContext createSecurityContext(WithMockCustomUser customUser) {
		SecurityContext context = SecurityContextHolder.createEmptyContext();

		Member member = Member.builder()
			.id(customUser.id())
			.email(customUser.username())
			.password("password")
			.name("홍길동")
			.nickname("길동이")
			.state(MemberState.ACTIVE)
			.createdAt(LocalDateTime.now())
			.updatedAt(LocalDateTime.now())
			.build();

		UserDetailsImpl userDetails = new UserDetailsImpl(member);
		UsernamePasswordAuthenticationToken authentication =
			new UsernamePasswordAuthenticationToken(userDetails, null, Collections.emptyList());
		context.setAuthentication(authentication);
		return context;
	}
}
