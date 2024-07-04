package com.example.mini.domain.like.fixture;

import com.example.mini.domain.like.entity.Like;
import com.example.mini.domain.member.entity.Member;
import com.example.mini.domain.accomodation.entity.Accomodation;

public class LikeEntityFixture {

	public static Like getLike(Member member, Accomodation accomodation) {
		return Like.builder()
			.member(member)
			.accomodation(accomodation)
			.isLiked(true)
			.build();
	}
}
