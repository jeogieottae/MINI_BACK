package com.example.mini.domain.member.fixture;

import com.example.mini.domain.member.entity.Member;
import com.example.mini.domain.member.entity.enums.MemberState;

public class MemberEntityFixture {

	public static Member getMember() {
		return Member.builder()
			.id(1L)
			.email("test@example.com")
			.name("홍길동")
			.nickname("길동이")
			.state(MemberState.ACTIVE)
			.build();
	}
}
