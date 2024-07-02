package com.example.mini.global.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.example.mini.domain.member.entity.enums.MemberState;
import com.example.mini.domain.member.model.request.ChangeNicknameRequest;
import com.example.mini.domain.member.model.request.LoginRequest;
import com.example.mini.domain.member.model.request.RegisterRequest;
import com.example.mini.domain.member.model.response.LoginResponse;
import com.example.mini.domain.member.model.response.UserProfileResponse;
import com.example.mini.global.auth.fixture.AuthServiceTestFixture;
import com.example.mini.global.auth.service.AuthService;
import com.example.mini.global.security.jwt.JwtProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest { // 수정해야함

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private AuthService authService;

	@MockBean
	private JwtProvider jwtProvider;

	private RegisterRequest registerRequest;
	private LoginRequest loginRequest;
	private ChangeNicknameRequest changeNicknameRequest;

	@BeforeEach
	void setUp() {
		registerRequest = AuthServiceTestFixture.createRegisterRequest();
		loginRequest = AuthServiceTestFixture.createLoginRequest();
		changeNicknameRequest = AuthServiceTestFixture.createChangeNicknameRequest("newnickname");
	}

	@Test
	@DisplayName("회원가입 성공")
	void registerSuccess() throws Exception {
		doNothing().when(authService).register(any(RegisterRequest.class));

		mockMvc.perform(post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"test@example.com\",\"password\":\"password\",\"name\":\"testname\",\"nickname\":\"testuser\"}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("회원가입 성공"));

		verify(authService).register(any(RegisterRequest.class));
	}

	@Test
	@DisplayName("로그인 성공")
	void loginSuccess() throws Exception {
		LoginResponse loginResponse = new LoginResponse(MemberState.ACTIVE, "accessToken", "refreshToken");
		when(authService.login(any(LoginRequest.class))).thenReturn(loginResponse);

		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"test@example.com\",\"password\":\"password\"}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("로그인 성공"))
			.andExpect(jsonPath("$.data.accessToken").value("accessToken"));

		verify(authService).login(any(LoginRequest.class));
	}


	@Test
	@DisplayName("로그아웃 성공")
	void logoutSuccess() throws Exception {
		when(authService.logout(any(HttpServletRequest.class), any(HttpServletResponse.class))).thenReturn("redirectUri");

		mockMvc.perform(get("/api/auth/logout"))
			.andExpect(status().is3xxRedirection());

		verify(authService).logout(any(HttpServletRequest.class), any(HttpServletResponse.class));
	}

	@Test
	@DisplayName("토큰 갱신 성공")
	void refreshTokenSuccess() throws Exception {
		doNothing().when(authService).refreshToken(any(HttpServletRequest.class), any(HttpServletResponse.class));

		mockMvc.perform(get("/api/auth/token/refresh"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("엑세스 토큰 재발급 성공"));

		verify(authService).refreshToken(any(HttpServletRequest.class), any(HttpServletResponse.class));
	}

	@Test
	@DisplayName("회원 탈퇴 성공")
	void withdrawSuccess() throws Exception {
		when(authService.withdraw(any(HttpServletRequest.class), any(HttpServletResponse.class))).thenReturn("redirectUri");

		mockMvc.perform(delete("/api/auth/withdraw"))
			.andExpect(status().is3xxRedirection());

		verify(authService).withdraw(any(HttpServletRequest.class), any(HttpServletResponse.class));
	}

	@Test
	@DisplayName("닉네임 변경 성공")
	void changeNicknameSuccess() throws Exception {
		doNothing().when(authService).updateNickname(any(HttpServletRequest.class), any(ChangeNicknameRequest.class));

		mockMvc.perform(put("/api/auth/nickname")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"nickname\":\"newnickname\"}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("닉네임 변경 성공"));

		verify(authService).updateNickname(any(HttpServletRequest.class), any(ChangeNicknameRequest.class));
	}

	@Test
	@DisplayName("유저 정보 조회 성공")
	void userInfoSuccess() throws Exception {
		UserProfileResponse userProfileResponse = new UserProfileResponse("testname", "testuser", "test@example.com");
		when(authService.getUserInfo(any(HttpServletRequest.class))).thenReturn(userProfileResponse);

		mockMvc.perform(get("/api/auth/userInfo"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("사용자 정보 조회 성공"))
			.andExpect(jsonPath("$.data.email").value("test@example.com"));

		verify(authService).getUserInfo(any(HttpServletRequest.class));
	}

	@Test
	@DisplayName("로그인 여부 확인 성공")
	void isLoggedInSuccess() throws Exception {
		when(authService.isLoggedIn(any(HttpServletRequest.class))).thenReturn(true);

		mockMvc.perform(get("/api/auth/isLoggedIn"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("성공"))
			.andExpect(jsonPath("$.data").value(true));

		verify(authService).isLoggedIn(any(HttpServletRequest.class));
	}
}
