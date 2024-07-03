package com.example.mini.domain.member.fixture;

import com.example.mini.domain.member.entity.Member;
import com.example.mini.domain.member.entity.enums.MemberState;
import com.example.mini.global.auth.model.GoogleUserInfo;
import com.example.mini.global.auth.model.KakaoUserInfo;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class MemberEntityFixture {

	public static Member getMember() {
		return Member.builder()
			.id(1L)
			.email("test@example.com")
			.password("password")
			.name("홍길동")
			.nickname("길동이")
			.state(MemberState.ACTIVE)
			.createdAt(LocalDateTime.now())
			.updatedAt(LocalDateTime.now())
			.build();
	}

	public static Map<String, Object> getGoogleAttributes() {
		Map<String, Object> attributes = new HashMap<>();
		attributes.put("sub", "123456789");
		attributes.put("email", "test@example.com");
		attributes.put("email_verified", true);
		attributes.put("name", "홍길동");
		attributes.put("given_name", "길동");
		attributes.put("family_name", "홍");
		attributes.put("picture", "https://example.com/profile.jpg");
		attributes.put("locale", "ko");
		return attributes;
	}

	public static GoogleUserInfo getGoogleUserInfo() {
		return new GoogleUserInfo(getGoogleAttributes());
	}


	public static Map<String, Object> getKakaoAttributes() {
		Map<String, Object> attributes = new HashMap<>();
		Map<String, Object> kakaoAccount = new HashMap<>();
		Map<String, Object> profile = new HashMap<>();

		profile.put("nickname", "홍길동");
		kakaoAccount.put("email", "test@example.com");
		kakaoAccount.put("profile", profile);

		attributes.put("id", 123456789L);
		attributes.put("kakao_account", kakaoAccount);

		return attributes;
	}

	public static KakaoUserInfo getKakaoUserInfo() {
		return new KakaoUserInfo(getKakaoAttributes());
	}
}
