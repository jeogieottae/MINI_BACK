package com.example.mini.global.auth.controller;

import com.example.mini.domain.member.model.request.LoginRequest;
import com.example.mini.domain.member.model.request.RegisterRequest;
import com.example.mini.domain.member.model.response.LoginResponse;
import com.example.mini.domain.member.repository.MemberRepository;
import com.example.mini.global.auth.oauth2.model.KakaoUserInfo;
import com.example.mini.global.auth.service.AuthService;
import com.example.mini.global.security.jwt.JwtProvider;
import com.example.mini.global.security.jwt.TokenService;
import com.example.mini.global.security.jwt.TokenType;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;
	private final JwtProvider jwtProvider;
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
	public ResponseEntity<String> logout(HttpServletRequest request) {
		String accessToken = jwtProvider.resolveToken(request);

		authService.logout(accessToken);
		return new ResponseEntity<>("Logged out successfully", HttpStatus.NO_CONTENT);
	}

	private KakaoUserInfo getKakaoUserInfo(String kakaoAccessToken) {
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "Bearer " + kakaoAccessToken);
		HttpEntity<String> entity = new HttpEntity<>("", headers);

		ResponseEntity<Map> response = restTemplate.exchange(
			"https://kapi.kakao.com/v2/user/me",
			HttpMethod.GET,
			entity,
			Map.class
		);

		Map<String, Object> attributes = response.getBody();
		return new KakaoUserInfo(attributes);
	}

	@GetMapping("/login/kakao")
	public ResponseEntity<LoginResponse> loginKakao(@RequestParam(name = "accessToken") String kakaoAccessToken) {
		KakaoUserInfo kakaoUserInfo = getKakaoUserInfo(kakaoAccessToken);
		String email = kakaoUserInfo.getEmail();

		// 사용자 정보를 기반으로 JWT 토큰 생성
		String jwtToken = jwtProvider.createToken(email, TokenType.ACCESS);

		// 리프레시 토큰 생성 및 저장
		String refreshToken = jwtProvider.createToken(email, TokenType.REFRESH);
		tokenService.saveRefreshToken(email, refreshToken);

		return ResponseEntity.ok(new LoginResponse(jwtToken, refreshToken));
	}
}