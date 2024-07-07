package com.example.mini.global.auth.controller;

import com.example.mini.domain.member.model.request.ChangeNicknameRequest;
import com.example.mini.domain.member.model.request.LoginRequest;
import com.example.mini.domain.member.model.request.RegisterRequest;
import com.example.mini.domain.member.model.response.LoginResponse;
import com.example.mini.domain.member.model.response.UserProfileResponse;
import com.example.mini.global.api.ApiResponse;
import com.example.mini.global.api.exception.success.SuccessCode;
import com.example.mini.global.auth.fixture.AuthcontrollerFixture;
import com.example.mini.global.auth.service.AuthService;
import com.example.mini.global.auth.service.StandardAuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {

	@InjectMocks
	private AuthController authController;

	@Mock
	private AuthService authService;

	@Mock
	private StandardAuthService standardAuthService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	@DisplayName("회원가입 성공")
	void registerSuccess() {
		RegisterRequest request = AuthcontrollerFixture.getRegisterRequest();
		doNothing().when(standardAuthService).register(any(RegisterRequest.class));

		ResponseEntity<ApiResponse<String>> response = authController.register(request);

		assertEquals(200, response.getStatusCodeValue());
		assertEquals(SuccessCode.OK.getHttpStatus(), response.getStatusCode());
	}

	@Test
	@DisplayName("로그인 성공")
	void loginSuccess() {
		LoginRequest request = AuthcontrollerFixture.getLoginRequest();
		LoginResponse loginResponse = AuthcontrollerFixture.getLoginResponse();
		when(standardAuthService.login(any(LoginRequest.class))).thenReturn(loginResponse);

		MockHttpServletResponse response = new MockHttpServletResponse();
		ResponseEntity<ApiResponse<LoginResponse>> responseEntity = authController.login(request, response);

		assertEquals(200, responseEntity.getStatusCodeValue());
		assertEquals(SuccessCode.OK.getHttpStatus(), responseEntity.getStatusCode());
		assertEquals(loginResponse, responseEntity.getBody().getBody());
	}

	@Test
	@DisplayName("로그아웃 성공")
	void logoutSuccess() throws IOException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		when(authService.logout(request, response)).thenReturn("http://example.com");

		authController.logout(request, response);

		assertEquals("http://example.com", response.getRedirectedUrl());
	}

	@Test
	@DisplayName("토큰 갱신 성공")
	void refreshTokenSuccess() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		when(authService.refreshToken(request, response)).thenReturn("newAccessToken");

		ResponseEntity<ApiResponse<String>> responseEntity = authController.refreshToken(request, response);

		assertEquals(200, responseEntity.getStatusCodeValue());
		assertEquals(SuccessCode.TOKEN_REFRESHED.getHttpStatus(), responseEntity.getStatusCode());
	}

	@Test
	@DisplayName("회원 탈퇴 성공")
	void withdrawSuccess() throws IOException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		when(authService.withdraw(request, response)).thenReturn("http://example.com");

		authController.withdraw(request, response);

		assertEquals("http://example.com", response.getRedirectedUrl());
	}

	@Test
	@DisplayName("닉네임 변경 성공")
	void changeNicknameSuccess() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		ChangeNicknameRequest changeRequest = new ChangeNicknameRequest("newNickname");
		doNothing().when(authService).updateNickname(request, changeRequest);

		ResponseEntity<ApiResponse<String>> responseEntity = authController.changeNickname(request, changeRequest);

		assertEquals(200, responseEntity.getStatusCodeValue());
		assertEquals(SuccessCode.NICKNAME_UPDATED.getHttpStatus(), responseEntity.getStatusCode());
	}

	@Test
	@DisplayName("사용자 정보 조회 성공")
	void userInfoSuccess() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		UserProfileResponse userInfo = AuthcontrollerFixture.getUserProfileResponse();
		when(authService.getUserInfo(request)).thenReturn(userInfo);

		ResponseEntity<ApiResponse<UserProfileResponse>> responseEntity = authController.userInfo(request);

		assertEquals(200, responseEntity.getStatusCodeValue());
		assertEquals(SuccessCode.USER_INFO_RETRIEVED.getHttpStatus(), responseEntity.getStatusCode());
		assertEquals(userInfo, responseEntity.getBody().getBody());
	}

	@Test
	@DisplayName("로그인 상태 확인 성공")
	void isLoggedInSuccess() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		when(authService.isLoggedIn(request)).thenReturn(true);

		ResponseEntity<ApiResponse<Boolean>> responseEntity = authController.isLoggedIn(request);

		assertEquals(200, responseEntity.getStatusCodeValue());
		assertEquals(SuccessCode.OK.getHttpStatus(), responseEntity.getStatusCode());
		assertTrue(responseEntity.getBody().getBody());
	}
}