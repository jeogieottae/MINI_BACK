package com.example.mini.global.auth.controller;

import com.example.mini.domain.member.entity.enums.MemberState;
import com.example.mini.domain.member.model.request.LoginRequest;
import com.example.mini.domain.member.model.request.RegisterRequest;
import com.example.mini.domain.member.model.response.LoginResponse;
import com.example.mini.global.api.ApiResponse;
import com.example.mini.global.api.exception.GlobalException;
import com.example.mini.global.auth.oauth2.model.KakaoUserInfo;
import com.example.mini.global.auth.service.AuthService;
import com.example.mini.global.api.exception.error.AuthErrorCode;
import com.example.mini.global.security.jwt.JwtProvider;
import com.example.mini.global.security.jwt.TokenService;
import com.example.mini.global.security.jwt.TokenType;
import com.example.mini.global.util.cookies.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

	private final AuthService authService;
	private final JwtProvider jwtProvider;
	private final TokenService tokenService;

	@PostMapping("/register")
	public ResponseEntity<ApiResponse<String>> register(@RequestBody RegisterRequest request) {
		String response = authService.register(request);
		return ResponseEntity.ok(ApiResponse.CREATED(response));
	}

	@PostMapping("/login")
	public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request, HttpServletResponse response) {
		LoginResponse loginResponse = authService.login(request);

		CookieUtil.addCookie(response, "accessToken", loginResponse.getAccessToken(), TokenType.ACCESS.getExpireTime() / 1000);
		CookieUtil.addCookie(response, "refreshToken", loginResponse.getRefreshToken(), TokenType.REFRESH.getExpireTime() / 1000);

		return ResponseEntity.ok(ApiResponse.OK(LoginResponse.builder().state(loginResponse.getState()).build()));
	}

	@PostMapping("/logout")
	public ResponseEntity<ApiResponse<String>> logout(HttpServletRequest request, HttpServletResponse response) {
		String accessToken = jwtProvider.resolveToken(request);

		authService.logout(accessToken);

		CookieUtil.deleteCookie(response, "accessToken");
		CookieUtil.deleteCookie(response, "refreshToken");

		return ResponseEntity.ok(ApiResponse.DELETE());
	}

	@PostMapping("/token/refresh")
	public ResponseEntity<ApiResponse<String>> refreshToken(HttpServletRequest request, HttpServletResponse response) {
		Cookie refreshTokenCookie = CookieUtil.getCookie(request, "refreshToken");
		if (refreshTokenCookie == null) {
			throw new GlobalException(AuthErrorCode.REFRESH_TOKEN_NOT_FOUND);
		}

		String newAccessToken = authService.createAccessToken(refreshTokenCookie.getValue());

		CookieUtil.addCookie(response, "accessToken", newAccessToken, TokenType.ACCESS.getExpireTime() / 1000);

		log.info("재발급된 Access 토큰을 쿠키에 저장: NewAccessToken={}", newAccessToken);

		return ResponseEntity.ok(ApiResponse.OK("Access token refreshed"));
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
	public ResponseEntity<ApiResponse<LoginResponse>> loginKakao(@RequestParam(name = "accessToken") String kakaoAccessToken) {
		KakaoUserInfo kakaoUserInfo = getKakaoUserInfo(kakaoAccessToken);
		String email = kakaoUserInfo.getEmail();

		// 사용자 정보를 기반으로 JWT 토큰 생성
		String accessToken = jwtProvider.createToken(email, TokenType.ACCESS, true);

		// 리프레시 토큰 생성 및 저장
		String refreshToken = jwtProvider.createToken(email, TokenType.REFRESH, true);
		tokenService.saveRefreshToken(email, refreshToken);

		return ResponseEntity.ok(ApiResponse.OK(LoginResponse.builder().state(MemberState.ACTIVE).build()));
	}

}
