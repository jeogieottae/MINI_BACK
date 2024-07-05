//package com.example.mini.global.auth.controller;
//
//import com.example.mini.domain.member.model.request.ChangeNicknameRequest;
//import com.example.mini.domain.member.model.request.LoginRequest;
//import com.example.mini.domain.member.model.request.RegisterRequest;
//import com.example.mini.domain.member.model.response.LoginResponse;
//import com.example.mini.domain.member.model.response.UserProfileResponse;
//import com.example.mini.global.api.exception.success.SuccessCode;
//import com.example.mini.global.auth.fixture.AuthcontrollerFixture;
//import com.example.mini.global.auth.service.AuthService;
//import com.example.mini.global.auth.service.StandardAuthService;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//
//import static org.mockito.Mockito.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//class AuthControllerTest {
//
//	private MockMvc mockMvc;
//
//	@Mock
//	private AuthService authService;
//
//	@Mock
//	private StandardAuthService standardAuthService;
//
//	@InjectMocks
//	private AuthController authController;
//
//	@BeforeEach
//	void setUp() {
//		MockitoAnnotations.openMocks(this);
//		mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
//	}
//
//	@Test
//	void register_ShouldReturnSuccess() throws Exception {
//		RegisterRequest request = AuthcontrollerFixture.getRegisterRequest();
//
//		mockMvc.perform(post("/api/auth/register")
//						.contentType(MediaType.APPLICATION_JSON)
//						.content(new ObjectMapper().writeValueAsString(request)))
//				.andExpect(status().isOk())
//				.andExpect(jsonPath("$.code").value(SuccessCode.REGISTER.getHttpStatus()))
//				.andExpect(jsonPath("$.message").value(SuccessCode.REGISTER.getDescription()));
//
//		verify(standardAuthService).register(any(RegisterRequest.class));
//	}
//
//	@Test
//	void login_ShouldReturnLoginResponse() throws Exception {
//		LoginRequest request = AuthcontrollerFixture.getLoginRequest();
//		LoginResponse response = AuthcontrollerFixture.getLoginResponse();
//
//		when(standardAuthService.login(any(LoginRequest.class))).thenReturn(response);
//
//		mockMvc.perform(post("/api/auth/login")
//						.contentType(MediaType.APPLICATION_JSON)
//						.content(new ObjectMapper().writeValueAsString(request)))
//				.andExpect(status().isOk())
//				.andExpect(jsonPath("$.code").value(SuccessCode.LOGIN.getHttpStatus()))
//				.andExpect(jsonPath("$.message").value(SuccessCode.LOGIN.getDescription()))
//				.andExpect(jsonPath("$.data.accessToken").value("accessToken"))
//				.andExpect(jsonPath("$.data.refreshToken").value("refreshToken"));
//
//		verify(standardAuthService).login(any(LoginRequest.class));
//		verify(standardAuthService).addTokenCookies(any(), eq("accessToken"), eq("refreshToken"));
//	}
//
//	@Test
//	void logout_ShouldRedirectAndReturnSuccess() throws Exception {
//		when(authService.logout(any(), any())).thenReturn("http://example.com");
//
//		mockMvc.perform(get("/api/auth/logout"))
//				.andExpect(status().isOk())
//				.andExpect(jsonPath("$.code").value(SuccessCode.LOGOUT.getHttpStatus()))
//				.andExpect(jsonPath("$.message").value(SuccessCode.LOGOUT.getDescription()));
//
//		verify(authService).logout(any(), any());
//	}
//
//	@Test
//	void refreshToken_ShouldReturnSuccess() throws Exception {
//		mockMvc.perform(get("/api/auth/token/refresh"))
//				.andExpect(status().isOk())
//				.andExpect(jsonPath("$.code").value(SuccessCode.TOKEN_REFRESHED.getHttpStatus()))
//				.andExpect(jsonPath("$.message").value(SuccessCode.TOKEN_REFRESHED.getDescription()));
//
//		verify(authService).refreshToken(any(), any());
//	}
//
//	@Test
//	void withdraw_ShouldRedirectAndReturnSuccess() throws Exception {
//		when(authService.withdraw(any(), any())).thenReturn("http://example.com");
//
//		mockMvc.perform(delete("/api/auth/withdraw"))
//				.andExpect(status().isOk())
//				.andExpect(jsonPath("$.code").value(SuccessCode.WITHDRAW.getHttpStatus()))
//				.andExpect(jsonPath("$.message").value(SuccessCode.WITHDRAW.getDescription()));
//
//		verify(authService).withdraw(any(), any());
//	}
//
//	@Test
//	void changeNickname_ShouldReturnSuccess() throws Exception {
//		ChangeNicknameRequest request = new ChangeNicknameRequest("newNickname");
//
//		mockMvc.perform(put("/api/auth/nickname")
//						.contentType(MediaType.APPLICATION_JSON)
//						.content(new ObjectMapper().writeValueAsString(request)))
//				.andExpect(status().isOk())
//				.andExpect(jsonPath("$.code").value(SuccessCode.NICKNAME_UPDATED.getHttpStatus()))
//				.andExpect(jsonPath("$.message").value(SuccessCode.NICKNAME_UPDATED.getDescription()));
//
//		verify(authService).updateNickname(any(), any(ChangeNicknameRequest.class));
//	}
//
//	@Test
//	void userInfo_ShouldReturnUserProfileResponse() throws Exception {
//		UserProfileResponse userInfo = AuthcontrollerFixture.getUserProfileResponse();
//		when(authService.getUserInfo(any())).thenReturn(userInfo);
//
//		mockMvc.perform(get("/api/auth/userInfo"))
//				.andExpect(status().isOk())
//				.andExpect(jsonPath("$.code").value(SuccessCode.USER_INFO_RETRIEVED.getHttpStatus()))
//				.andExpect(jsonPath("$.message").value(SuccessCode.USER_INFO_RETRIEVED.getDescription()))
//				.andExpect(jsonPath("$.data.email").value("test@example.com"))
//				.andExpect(jsonPath("$.data.nickname").value("nickname"));
//
//		verify(authService).getUserInfo(any());
//	}
//
//	@Test
//	void isLoggedIn_ShouldReturnBoolean() throws Exception {
//		when(authService.isLoggedIn(any())).thenReturn(true);
//
//		mockMvc.perform(get("/api/auth/isLoggedIn"))
//				.andExpect(status().isOk())
//				.andExpect(jsonPath("$.code").value(SuccessCode.OK.getHttpStatus()))
//				.andExpect(jsonPath("$.message").value(SuccessCode.OK.getDescription()))
//				.andExpect(jsonPath("$.data").value(true));
//
//		verify(authService).isLoggedIn(any());
//	}
//}