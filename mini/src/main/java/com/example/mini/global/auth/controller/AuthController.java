package com.example.mini.global.auth.controller;

import com.example.mini.domain.member.model.request.LoginRequest;
import com.example.mini.domain.member.model.request.RegisterRequest;
import com.example.mini.domain.member.model.response.LoginResponse;
import com.example.mini.global.api.ApiResponse;
import com.example.mini.global.api.exception.GlobalException;
import com.example.mini.global.auth.service.AuthService;
import com.example.mini.global.api.exception.error.AuthErrorCode;
import com.example.mini.global.security.jwt.JwtProvider;
import com.example.mini.global.security.jwt.TokenType;
import com.example.mini.global.util.cookies.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

	private final AuthService authService;
	private final JwtProvider jwtProvider;

	@Value("${server.ssl.enabled:false}")
	private boolean isSecure;

	@PostMapping("/register")
	public ResponseEntity<ApiResponse<String>> register(@RequestBody RegisterRequest request) {
		String response = authService.register(request);
		return ResponseEntity.ok(ApiResponse.CREATED(response));
	}

	@PostMapping("/login")
	public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request, HttpServletResponse response) {
		LoginResponse loginResponse = authService.login(request);

		CookieUtil.addCookie(response, "accessToken", loginResponse.getAccessToken(), TokenType.ACCESS.getExpireTime() / 1000, isSecure);
		CookieUtil.addCookie(response, "refreshToken", loginResponse.getRefreshToken(), TokenType.REFRESH.getExpireTime() / 1000, isSecure);

		return ResponseEntity.ok(ApiResponse.OK(LoginResponse.builder().state(loginResponse.getState()).build()));
	}

	@PostMapping("/logout")
	public ResponseEntity<ApiResponse<String>> logout(HttpServletRequest request, HttpServletResponse response) {
		String accessToken = jwtProvider.resolveToken(request);

		authService.logout(accessToken);

		CookieUtil.deleteCookie(response, "accessToken", isSecure);
		CookieUtil.deleteCookie(response, "refreshToken", isSecure);

		// 세션 무효화
		request.getSession().invalidate();

		return ResponseEntity.ok(ApiResponse.DELETE());
	}

	@PostMapping("/token/refresh")
	public ResponseEntity<ApiResponse<String>> refreshToken(HttpServletRequest request, HttpServletResponse response) {
		Cookie refreshTokenCookie = CookieUtil.getCookie(request, "refreshToken");
		if (refreshTokenCookie == null) {
			throw new GlobalException(AuthErrorCode.REFRESH_TOKEN_NOT_FOUND);
		}

		String newAccessToken = authService.createAccessToken(refreshTokenCookie.getValue());

		CookieUtil.addCookie(response, "accessToken", newAccessToken, TokenType.ACCESS.getExpireTime() / 1000, isSecure);

		log.info("재발급된 Access 토큰을 쿠키에 저장: NewAccessToken={}", newAccessToken);

		return ResponseEntity.ok(ApiResponse.OK("Access token refreshed"));
	}

}
