package com.example.mini.global.auth.fixture;

import com.example.mini.domain.member.entity.Member;
import com.example.mini.domain.member.entity.enums.MemberState;
import com.example.mini.domain.member.model.request.ChangeNicknameRequest;
import com.example.mini.domain.member.model.request.LoginRequest;
import com.example.mini.domain.member.model.request.RegisterRequest;
import com.example.mini.global.auth.model.GoogleUserInfo;
import com.example.mini.global.auth.model.TokenResponse;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.Map;

public class AuthServiceTestFixture {

	public static RegisterRequest createRegisterRequest() {
		return RegisterRequest.builder()
			.name("John Doe")
			.nickname("johndoe")
			.email("example@example.com")
			.password("password")
			.build();
	}

	public static LoginRequest createLoginRequest() {
		return LoginRequest.builder()
			.email("example@example.com")
			.password("password")
			.build();
	}


	public static ChangeNicknameRequest createChangeNicknameRequest(String newNickname) {
		return ChangeNicknameRequest.builder()
			.nickname(newNickname)
			.build();
	}

	public static TokenResponse createTokenResponse() {
		return TokenResponse.builder()
			.token_type("Bearer")
			.access_token("access_token")
			.id_token("id_token")
			.expires_in(3600)
			.refresh_token("refresh_token")
			.refresh_token_expires_in(3600)
			.build();
	}

	public static ChangeNicknameRequest createChangeNicknameRequest() {
		return ChangeNicknameRequest.builder()
			.nickname("newNickname")
			.build();
	}

	public static Map<String, Object> getAttributes() {
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
		return new GoogleUserInfo(getAttributes());
	}

	public static Member createMember() {
		return Member.builder()
			.email("test@example.com")
			.nickname("testNickname")
			.password("testPassword")
			.state(MemberState.ACTIVE)
			.build();
	}
}
