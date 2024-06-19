package com.example.mini.global.auth.controller;

import com.example.mini.domain.member.model.request.LoginRequest;
import com.example.mini.domain.member.model.request.LogoutRequest;
import com.example.mini.domain.member.model.request.RegisterRequest;
import com.example.mini.domain.member.model.response.LoginResponse;
import com.example.mini.global.auth.service.AuthService;
import com.example.mini.global.security.jwt.TokenService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;
	private final TokenService tokenService;

	@PostMapping("/register")
	public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
		String response = authService.register(request);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
		LoginResponse response = authService.login(request);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/logout")
	public ResponseEntity<String> logout(@RequestHeader("Authorization") String authHeader, @RequestBody LogoutRequest request) {
		String email = request.getEmail();
		String accessToken = authHeader.replace("Bearer ", "");
		authService.logout(email, accessToken);
		return ResponseEntity.ok("로그아웃 되었습니다.");
	}
}

