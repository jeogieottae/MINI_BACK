package com.example.mini.global.auth.fixture;

import com.example.mini.domain.member.entity.Member;
import com.example.mini.domain.member.entity.enums.MemberState;
import com.example.mini.domain.member.model.request.ChangeNicknameRequest;
import com.example.mini.domain.member.model.request.LoginRequest;
import com.example.mini.domain.member.model.request.RegisterRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

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
}
