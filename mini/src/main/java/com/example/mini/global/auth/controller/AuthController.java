package com.example.mini.global.auth.controller;

import com.example.mini.domain.member.entity.enums.MemberState;
import com.example.mini.domain.member.model.request.LoginRequest;
import com.example.mini.domain.member.model.request.RegisterRequest;
import com.example.mini.domain.member.model.response.LoginResponse;
import com.example.mini.global.auth.oauth2.model.UserInfo;
import com.example.mini.global.auth.service.AuthService;
import com.example.mini.global.exception.error.AuthErrorCode;
import com.example.mini.global.exception.type.GlobalException;
import com.example.mini.global.security.jwt.JwtProvider;
import com.example.mini.global.security.jwt.TokenService;
import com.example.mini.global.security.jwt.TokenType;
import com.example.mini.global.util.api.ApiResponse;
import com.example.mini.global.util.cookies.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

	private final AuthService authService;
	private final JwtProvider jwtProvider;
	private final ClientRegistrationRepository clientRegistrationRepository;

	@Value("${spring.security.oauth2.client.registration.kakao.client-id}")
	private String kakaoClientId;
	private String kakaoLogoutRedirectUri = "http://localhost:8080/api/protected/home";

	@Value("${spring.security.oauth2.client.registration.google.client-id}")
	private String googleClientId;
	private String googleLogoutRedirectUri = "http://localhost:8080/api/protected/home";

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

	@GetMapping("/login/google")
	public void googleLogin(HttpServletResponse response) throws IOException {
		redirectToLoginPage("google", response);
	}

	@GetMapping("/login/kakao")
	public void kakaoLogin(HttpServletResponse response) throws IOException {
		redirectToLoginPage("kakao", response);
	}


	@GetMapping("logout/google")
	public void googleLogout(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String googleLogoutUrl = "https://accounts.google.com/o/oauth2/revoke?token=";

		Cookie[] cookies = request.getCookies();
		String accessToken = null;
		if(cookies != null) {
			for(Cookie cookie : cookies) {
				if("google_token".equals(cookie.getName())) {
					accessToken = cookie.getValue();
					break;
				}
			}
		}

		if(accessToken == null) {
			// 엑세스 토큰을 못찾았을 때
			response.sendRedirect("/");
			return;
		}

		// 쿠키 삭제
		Cookie cookie = new Cookie("google_token", null);
		cookie.setMaxAge(0);
		cookie.setPath("/");
		response.addCookie(cookie);

		// JSESSIONID 쿠키 삭제
		Cookie jsessionidCookie = new Cookie("JSESSIONID", null);
		jsessionidCookie.setMaxAge(0);
		jsessionidCookie.setPath("/");
		response.addCookie(jsessionidCookie);

		// 세션 무효화
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.invalidate();
		}

		response.sendRedirect(googleLogoutUrl + accessToken +
				"&client_id=" + googleClientId +
				"&post_logout_redirect_uri=" + googleLogoutRedirectUri);

	}

	@GetMapping("logout/kakao")
	public void kakaoLogout(HttpServletRequest request, HttpServletResponse response) throws IOException {
		// 쿠키 삭제
		Cookie cookie = new Cookie("kakao_token", null);
		cookie.setMaxAge(0);
		cookie.setPath("/");
		response.addCookie(cookie);

		// JSESSIONID 쿠키 삭제
		Cookie jsessionidCookie = new Cookie("JSESSIONID", null);
		jsessionidCookie.setMaxAge(0);
		jsessionidCookie.setPath("/");
		response.addCookie(jsessionidCookie);

		// 세션 무효화
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.invalidate();
		}

		response.sendRedirect("https://kauth.kakao.com/oauth/logout?client_id=" + kakaoClientId
				+ "&logout_redirect_uri=" + kakaoLogoutRedirectUri);


	}

	private void redirectToLoginPage(String registrationId, HttpServletResponse response) throws IOException {
		ClientRegistration registration = clientRegistrationRepository.findByRegistrationId(registrationId);
		if (registration != null) {
			String authorizationRequestBaseUri = "/oauth2/authorization";
			String authorizationRequestUri = authorizationRequestBaseUri + "/" + registration.getRegistrationId();
			response.sendRedirect(authorizationRequestUri);
		} else {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "유효하지 않은 로그인 제공자입니다.");
		}
	}

}
