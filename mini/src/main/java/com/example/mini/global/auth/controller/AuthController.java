package com.example.mini.global.auth.controller;

import com.example.mini.domain.member.model.request.ChangeNicknameRequest;
import com.example.mini.domain.member.model.request.LoginRequest;
import com.example.mini.domain.member.model.request.RegisterRequest;
import com.example.mini.domain.member.model.response.LoginResponse;
import com.example.mini.domain.member.model.response.UserProfileResponse;
import com.example.mini.global.api.ApiResponse;
import com.example.mini.global.auth.service.AuthService;
import com.example.mini.global.security.jwt.JwtProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

	private final AuthService authService;
	private final JwtProvider jwtProvider;

	@PostMapping("/register")
	public ResponseEntity<ApiResponse<String>> register(@RequestBody RegisterRequest request) {
		String response = authService.register(request);
		return ResponseEntity.ok(ApiResponse.CREATED(response));
	}

	@PostMapping("/login")
	public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request, HttpServletResponse response) {
		LoginResponse loginResponse = authService.login(request);
		authService.addTokenCookies(response, loginResponse.getAccessToken(), loginResponse.getRefreshToken());
		return ResponseEntity.ok(ApiResponse.OK(loginResponse));
	}

	@GetMapping("/logout")
	public ResponseEntity<ApiResponse<String>> logout(HttpServletRequest request, HttpServletResponse response)
	throws IOException {
		String redirectUri = authService.logout(request, response);
		response.sendRedirect(redirectUri);
		return ResponseEntity.ok(ApiResponse.DELETE());
	}

	@GetMapping("/token/refresh")
	public ResponseEntity<ApiResponse<String>> refreshToken(HttpServletRequest request, HttpServletResponse response) {
		authService.refreshToken(request, response);
		return ResponseEntity.ok(ApiResponse.OK("Access token refreshed"));
	}

	@DeleteMapping("/withdraw")
	public ResponseEntity<ApiResponse<String>> withdraw(HttpServletRequest request, HttpServletResponse response)
		throws IOException {
		String redirectUri = authService.withdraw(request, response);
		response.sendRedirect(redirectUri);
		return ResponseEntity.ok(ApiResponse.DELETE());
	}

	@PutMapping("/nickname")
	public ResponseEntity<ApiResponse<String>> changeNickname(
		HttpServletRequest request, @RequestBody ChangeNicknameRequest changeNicknameRequest) {
		authService.updateNickname(request, changeNicknameRequest);
		return ResponseEntity.ok(ApiResponse.OK("닉네임이 성공적으로 변경되었습니다."));
	}

	@GetMapping("/userInfo")
	public ResponseEntity<ApiResponse<UserProfileResponse>> userInfo(HttpServletRequest request) {
		UserProfileResponse userInfo = authService.getUserInfo(request);
		return ResponseEntity.ok(ApiResponse.OK(userInfo));
	}
}
